package hi.block.entity;

public interface MonitorLaunchableBlockEntity {
    boolean hasLaunchPayload();

    int getLaunchPayloadCount();

    int getMaxLaunchPayloadCount();

    boolean launch(CameraBlockEntity camera);
}
