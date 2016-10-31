package pl.khuzzuk.messaging;

class MultiBagSubscriber extends AbstractMultiContentSubscriber<BagMessage> {
    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}
