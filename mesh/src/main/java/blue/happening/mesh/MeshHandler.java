package blue.happening.mesh;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import blue.happening.mesh.statistics.NetworkStats;

import blue.happening.mesh.statistics.NetworkStats;
import blue.happening.mesh.statistics.StatsResult;

public class MeshHandler {

    public static int INITIAL_MESSAGE_TQ = 255;
    public static int INITIAL_MESSAGE_TTL = 5;
    public static int HOP_PENALTY = 15;
    public static int OGM_INTERVAL = 2;
    public static int PURGE_INTERVAL = 200;
    public static int NETWORK_STAT_UPDATE_INTERVAL = 1;
    public static int SLIDING_WINDOW_SIZE = 12;
    public static int DEVICE_EXPIRATION = 200;

    private static final int INITIAL_MIN_SEQUENCE = 0;
    private static final int INITIAL_MAX_SEQUENCE = 1024;

    static final int MESSAGE_TYPE_OGM = 1;
    static final int MESSAGE_TYPE_UCM = 2;
    static final String BROADCAST_ADDRESS = "BROADCAST";


    private final RoutingTable routingTable;
    private final Router router;
    private final ILayerCallback layerCallback;
    private final String uuid;
    private IMeshHandlerCallback meshHandlerCallback;
    private int sequence;
    private NetworkStats ucmStats;
    private NetworkStats ogmStats;

    public MeshHandler(String uuid) {
        this.uuid = uuid;
        sequence = ThreadLocalRandom.current().nextInt(INITIAL_MIN_SEQUENCE, INITIAL_MAX_SEQUENCE);
        routingTable = new RoutingTable();
        router = new Router(routingTable, uuid);
        layerCallback = new LayerCallback();
        ucmStats = new NetworkStats();
        ogmStats = new NetworkStats();

        double currentTime = System.currentTimeMillis();
        ucmStats.updateTs(currentTime);
        ogmStats.updateTs(currentTime);

        router.addObserver(new RouterObserver(ogmStats, ucmStats));

        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(
                new OGMRunner(),
                ThreadLocalRandom.current().nextInt(OGM_INTERVAL),
                OGM_INTERVAL, TimeUnit.SECONDS);
        executor.scheduleAtFixedRate(
                new PurgeRunner(),
                ThreadLocalRandom.current().nextInt(PURGE_INTERVAL),
                PURGE_INTERVAL, TimeUnit.SECONDS);
        executor.scheduleAtFixedRate(
                new NetworkStatsUpdateRunner(),
                NETWORK_STAT_UPDATE_INTERVAL,
                NETWORK_STAT_UPDATE_INTERVAL, TimeUnit.SECONDS);
    }

    public void registerLayer(Layer layer) {
        layer.registerLayerCallback(layerCallback);
    }

    public void registerCallback(IMeshHandlerCallback callback) {
        meshHandlerCallback = callback;
        routingTable.registerMeshHandlerCallback(callback);
    }

    public RoutingTable getRoutingTable() {
        return routingTable;
    }

    public List<MeshDevice> getDevices() {
        return routingTable.getReachableMeshDevices();
    }

    public boolean sendMessage(byte[] message, String uuid) {
        RemoteDevice remoteDevice = routingTable.get(uuid);
        if (remoteDevice == null) {
            System.out.println("Mesh handler couldn't find " + uuid + " in routing table");
            return false;
        } else {
            Message ucm = new Message(this.uuid, uuid, INITIAL_MIN_SEQUENCE, MESSAGE_TYPE_UCM, message);
            try {
                ucm = router.routeMessage(ucm);
            } catch (Router.RoutingException e) {
                e.printStackTrace();
            }
            return ucm != null;
        }
    }

    private class OGMRunner implements Runnable {
        @Override
        public void run() {
            try {
                Message message = new Message(uuid, BROADCAST_ADDRESS, sequence, MESSAGE_TYPE_OGM, null);
                for (RemoteDevice remoteDevice : routingTable.getNeighbours()) {
                    remoteDevice.sendMessage(message);
                    remoteDevice.getEchoSlidingWindow().slideSequence(sequence);
                }
                sequence++;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private class PurgeRunner implements Runnable {
        @Override
        public void run() {
            try {
                for (RemoteDevice remoteDevice : routingTable.getExpiredRemoteDevices()) {
                    routingTable.remove(remoteDevice.getUuid());
                    System.out.println("REMOVE REMOTE DEVICE " + remoteDevice + " BECAUSE IT IS EXPIRED!");
                    remoteDevice.remove();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private class NetworkStatsUpdateRunner implements Runnable {
        @Override
        public void run() {
            try {
                double currentTime = System.currentTimeMillis();
                StatsResult networkStat = new StatsResult();

                networkStat.setOgmIncoming(ogmStats.getIncomingStat());
                networkStat.setOgmOutgoing(ogmStats.getOutgoingStat());
                networkStat.setUcmIncoming(ucmStats.getIncomingStat());
                networkStat.setUcmOutgoing(ucmStats.getOutgoingStat());

                meshHandlerCallback.onNetworkStatsUpdated(networkStat);

                ucmStats.updateTs(currentTime);
                ogmStats.updateTs(currentTime);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private class LayerCallback implements ILayerCallback {

        @Override
        public void onDeviceAdded(RemoteDevice remoteDevice) {
            routingTable.ensureConnection(remoteDevice, remoteDevice);
        }

        @Override
        public void onDeviceRemoved(RemoteDevice remoteDevice) {
            routingTable.removeAsNeighbour(remoteDevice.getUuid());
        }

        @Override
        public void onMessageReceived(byte[] bytes) {
            Message message, propagate;

            try {
                message = Message.fromBytes(bytes);
                if (message == null) {
                    throw new Exception("Could'nt parse message");
                }
            } catch (Exception e) {
                System.out.println("Message broken: " + e.getMessage());
                return;
            }

            if (message.getType() == MESSAGE_TYPE_OGM) {
                ogmStats.addInComingMessage(message);
            } else if (message.getType() == MESSAGE_TYPE_UCM) {
                ucmStats.addInComingMessage(message);
            }

            try {
                propagate = router.routeMessage(message);
            } catch (Router.RoutingException e) {
                System.out.println("Routing failed: " + e.getMessage());
                return;
            }

            if (propagate != null) {
                MeshDevice source = routingTable.get(message.getSource()).getMeshDevice();
                meshHandlerCallback.onMessageReceived(message.getBody(), source);
            }

            // Check whether message is an echo OGM
            if (!message.getSource().equals(uuid)) {
                RemoteDevice source = routingTable.get(message.getSource());
                if (source != null) {
                    // TODO: Move this block to a better location
                    MeshDevice meshDevice = source.getMeshDevice();
                    meshDevice.setReceivedSize(meshDevice.getReceivedSize() + message.toBytes().length);
                    meshHandlerCallback.onDeviceUpdated(meshDevice);
                }
            }
        }
    }
}
