import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Assignment {

    public static void main(String[] args) {
        // inputs
        int simulationDuration = 1800;
        int averageArrivalRate = 2;
        int averageServiceRate = 10;
        // run simulation
        new Simulator(simulationDuration, averageArrivalRate, averageServiceRate,
                Arrays.asList(
                        SingleQueueProcessor.NAME,
                        RoundRobinQueueProcessor.NAME,
                        ShortestQueueProcessor.NAME,
                        RandomQueueProcessor.NAME)
        ).runSimulation();
    }
}

class Simulator {

    private static final int NUMBER_OF_STATIONS = 5;

    private final int simulationDuration;
    private final int averageArrivalRate;
    private final int averageServiceRate;
    private final List<String> processorNames;

    private final CountDownLatch simulationLatch;

    public Simulator(final int simulationDuration, final int averageArrivalRate, final int averageServiceRate, final List<String> processorNames) {
        this.simulationDuration = simulationDuration;
        this.averageArrivalRate = averageArrivalRate;
        this.averageServiceRate = averageServiceRate;
        this.processorNames = processorNames;
        this.simulationLatch = new CountDownLatch(processorNames.size());
    }

    public void runSimulation() {
        final List<QueueProcessor> processors = processorNames.stream()
                .map(processorName -> {
                    final QueueProcessor queueProcessor = getProcessor(processorName, System.currentTimeMillis());
                    queueProcessor.startProcessing();
                    return queueProcessor;
                })
                .collect(Collectors.toList());
        try {
            simulationLatch.await();
        } catch (final InterruptedException exception) {
            Thread.currentThread().interrupt();
            exception.printStackTrace();
        }
        processors.forEach(QueueProcessor::summarizeSimulationWithLatch);
    }

    private QueueProcessor<Passenger> getProcessor(final String processorName, final long startTime) {
        if (SingleQueueProcessor.NAME.equals(processorName)) {
            return new SingleQueueProcessor<>(NUMBER_OF_STATIONS, averageServiceRate, averageArrivalRate, startTime, simulationDuration * 1000L, simulationLatch);
        }
        if (RoundRobinQueueProcessor.NAME.equals(processorName)) {
            return new RoundRobinQueueProcessor(NUMBER_OF_STATIONS, averageServiceRate, averageArrivalRate, startTime, simulationDuration * 1000L, simulationLatch);
        }
        if (ShortestQueueProcessor.NAME.equals(processorName)) {
            return new ShortestQueueProcessor(NUMBER_OF_STATIONS, averageServiceRate, averageArrivalRate, startTime, simulationDuration * 1000L, simulationLatch);
        }
        if (RandomQueueProcessor.NAME.equals(processorName)) {
            return new RandomQueueProcessor(NUMBER_OF_STATIONS, averageServiceRate, averageArrivalRate, startTime, simulationDuration * 1000L, simulationLatch);
        }
        throw new RuntimeException("Processor cannot be found, processorName=" + processorName);
    }
}

class SingleQueueProcessor<T extends Passenger> extends QueueProcessor<T> {

    public static final String NAME = "SingleQueue";

    private final List<Station<T>> stations;

    public SingleQueueProcessor(final int numberOfStations, final int averageServiceRate, final int averageArrivalRate, final long startTime, final long simulationDuration, final CountDownLatch simulationLatch) {
        super(numberOfStations, averageServiceRate, averageArrivalRate, startTime, simulationDuration, simulationLatch);
        final StationQueue<T> stationQueue = new StationQueue<>(new ConcurrentLinkedQueue<>()); // shared queue for all stations
        stations = IntStream.range(0, numberOfStations)
                .mapToObj(i -> new Station<>(NAME + " Station " + i, stationQueue))
                .collect(Collectors.toList());
    }

    @Override
    protected StationQueue<T> getNextQueue() {
        return stations.get(0).getStationQueue(); // All the stations point the same queue
    }

    @Override
    public List<Station<T>> getStations() {
        return stations;
    }

    @Override
    protected String getProcessorType() {
        return NAME;
    }
}

class RoundRobinQueueProcessor extends MultiQueueProcessor<Passenger> {

    public static final String NAME = "RoundRobinQueue";

    private int nextQueueIndex = 0;

    public RoundRobinQueueProcessor(final int numberOfStations, final int averageServiceRate, final int averageArrivalRate, final long startTime, final long simulationDuration, final CountDownLatch simulationLatch) {
        super(numberOfStations, averageServiceRate, averageArrivalRate, startTime, simulationDuration, simulationLatch);
    }

    @Override
    protected StationQueue getNextQueue() {
        return getStations().get(nextQueueIndex++ % 5).getStationQueue();
    }

    @Override
    protected String getProcessorType() {
        return NAME;
    }
}

class ShortestQueueProcessor extends MultiQueueProcessor<Passenger> {

    public static final String NAME = "ShortestQueue";

    public ShortestQueueProcessor(final int numberOfStations, final int averageServiceRate, final int averageArrivalRate, final long startTime, final long simulationDuration, final CountDownLatch simulationLatch) {
        super(numberOfStations, averageServiceRate, averageArrivalRate, startTime, simulationDuration, simulationLatch);
    }

    @Override
    protected StationQueue getNextQueue() {
        return getStations().stream()
                .map(Station::getStationQueue)
                .min(Comparator.comparingInt(q -> q.getQueue().size()))
                .orElseThrow(() -> new RuntimeException("No queues found"));
    }

    @Override
    protected String getProcessorType() {
        return NAME;
    }
}

class RandomQueueProcessor extends MultiQueueProcessor<Passenger> {

    public static final String NAME = "RandomQueue";

    public RandomQueueProcessor(final int numberOfStations, final int averageServiceRate, final int averageArrivalRate, final long startTime, final long simulationDuration, final CountDownLatch simulationLatch) {
        super(numberOfStations, averageServiceRate, averageArrivalRate, startTime, simulationDuration, simulationLatch);
    }

    @Override
    protected StationQueue getNextQueue() {
        return getStations().get(random.nextInt(getStations().size())).getStationQueue();
    }

    @Override
    protected String getProcessorType() {
        return NAME;
    }
}

abstract class MultiQueueProcessor<T extends Passenger> extends QueueProcessor<T> {

    private final List<Station<T>> stations;

    public MultiQueueProcessor(final int numberOfStations, final int averageServiceRate, final int averageArrivalRate, final long startTime, final long simulationDuration, final CountDownLatch simulationLatch) {
        super(numberOfStations, averageServiceRate, averageArrivalRate, startTime, simulationDuration, simulationLatch);
        stations = IntStream.range(0, numberOfStations)
                .mapToObj(i -> new Station<T>(getProcessorType() + " Station " + i, new StationQueue<>(new ConcurrentLinkedQueue<>())))
                .collect(Collectors.toList());

    }

    @Override
    public List<Station<T>> getStations() {
        return stations;
    }
}

abstract class QueueProcessor<T extends Passenger> {

    protected final Random random = new Random();

    protected long endTime;

    private final int numberOfStations;
    private final int averageServiceTime;
    private final int averageArrivalRate;
    private final long startTime;
    private final long simulationDuration;

    private final ScheduledExecutorService executorService;
    private final CountDownLatch simulationLatch;
    private final CountDownLatch processorLatch;

    private ScheduledFuture<?> queueElementsTask;
    private List<ScheduledFuture<?>> processElementsTasks;

    public QueueProcessor(final int numberOfStations, final int averageServiceTime, final int averageArrivalRate, final long startTime, final long simulationDuration, final CountDownLatch simulationLatch) {
        // inputs
        this.numberOfStations = numberOfStations;
        this.averageServiceTime = averageServiceTime;
        this.averageArrivalRate = averageArrivalRate;
        this.startTime = startTime;
        this.simulationDuration = simulationDuration;
        this.simulationLatch = simulationLatch;
        // concurrency
        this.executorService = Executors.newScheduledThreadPool(numberOfStations + 1);
        this.processorLatch = new CountDownLatch(numberOfStations + 1); // we have a thread for each station task + queueing task
        this.processElementsTasks = new ArrayList<>();
    }

    public abstract List<Station<T>> getStations();

    public void summarizeSimulationWithLatch() {
        executorService.shutdown();
        System.out.println(getProcessorType() + " Simulation Summary");
        System.out.println("Total Processing Duration (Seconds) = " + ((endTime - startTime) / 1000L));
        System.out.println("Station Summary - ");
        getStations().stream()
                .forEach(station -> System.out.println(String.format("stationName=%s, occupancyRate=%s",
                        station.getName(), station.getTotalOccupancyTimePercentage(endTime - startTime) + "%")));
        System.out.println("Queue Summary - ");
        getStations().stream()
                .map(Station::getStationQueue)
                .distinct()
                .forEach(stationQueue -> System.out.println(String.format("numberOfProcessedElements=%s, maximumLength=%s, waitingTimeMax=%s, waitingTimeAvg=%s",
                        stationQueue.getNumberOfProcessedElements(), stationQueue.getMaxLength(), stationQueue.getWaitingTimeMaxInSeconds().toString(),
                        stationQueue.getWaitingTimeAvgInSeconds().toString())));
        System.out.println("-------------------------");
    }

    public void startProcessing() {
        queueElements();
        processElements();
        shutdown();
    }

    private void queueElements() {
        final AtomicLong arrivalTime = new AtomicLong(System.currentTimeMillis() + getRandomMillisecondsWithAverage(averageArrivalRate));
        final Runnable runnable = () -> {
            long currentTime = System.currentTimeMillis();
            if (currentTime >= (startTime + simulationDuration)) { // The queueing duration is over
                Optional.ofNullable(queueElementsTask)
                        .ifPresentOrElse(task -> {
                                    task.cancel(false);
                                    processorLatch.countDown();
                                },
                                () -> new RuntimeException("Cannot terminate queueElements because task is null"));
            } else if (currentTime >= arrivalTime.get()) { // Next element should be queued
                getNextQueue().offer(new Passenger(arrivalTime.get())); // queue the element
                arrivalTime.updateAndGet(currentArrivalTime -> currentArrivalTime + getRandomMillisecondsWithAverage(averageArrivalRate));
            }
        };
        queueElementsTask = executorService.scheduleWithFixedDelay(runnable, 0, 100, TimeUnit.MILLISECONDS);
    }

    private void processElements() {
        IntStream.range(0, numberOfStations)
                .forEach(i -> {
                    final Station<T> station = getStations().get(i);
                    final AtomicLong serviceTime = new AtomicLong(System.currentTimeMillis() + getRandomMillisecondsWithAverage(averageServiceTime));
                    final Runnable runnable = () -> {
                        final long currentTime = System.currentTimeMillis();
                        final boolean isProcessingComplete = currentTime >= (startTime + simulationDuration) // Current Time is after Simulation End Time
                                && currentTime >= serviceTime.get() // Current Time is after next Service End Time
                                && station.getStationQueue().getQueue().isEmpty(); // Queue is empty
                        if (isProcessingComplete) { // Processing is completed
                            Optional.ofNullable(processElementsTasks.get(i))
                                    .ifPresentOrElse(task -> {
                                                processorLatch.countDown();
                                                task.cancel(false);
                                            },
                                            () ->  new RuntimeException("Cannot terminate processElements because task is null"));
                        } else if (currentTime >= serviceTime.get()) { // Station is ready to process
                            Optional.ofNullable(station.getStationQueue().getQueue().poll())
                                    .ifPresent(element -> {
                                        final long nextServiceTime = getRandomMillisecondsWithAverage(averageServiceTime);
                                        station.getStationQueue().process(element, currentTime);
                                        station.updateTotalOccupancyTime(nextServiceTime);
                                        serviceTime.updateAndGet(currentServiceTime -> currentServiceTime + nextServiceTime);
                                    });
                        }
                    };
                    processElementsTasks.add(i, executorService.scheduleWithFixedDelay(runnable, 0, 100, TimeUnit.MILLISECONDS));
                });
    }

    private void shutdown() {
        executorService.scheduleWithFixedDelay(() -> {
            if (processorLatch.getCount() == 0) {
                this.endTime = System.currentTimeMillis(); // mark the processing end time for stats
                executorService.shutdown(); // shut down this processor
                simulationLatch.countDown(); // mark this processor completed in simulation
            }
        }, 0, 1, TimeUnit.MILLISECONDS);
    }

    protected abstract StationQueue getNextQueue();

    protected abstract String getProcessorType();

    private long getRandomMillisecondsWithAverage(final int average) {
        return (random.nextInt(average) + (average / 2)) * 1000L;
    }
}

class StationQueue<T extends Passenger> {

    private final Queue<T> queue;

    // Statistics which get updated as we process
    private final AtomicInteger maxLength = new AtomicInteger(0);
    private final AtomicInteger numberOfProcessedElements = new AtomicInteger(0);
    private final AtomicLong waitingTimeMax = new AtomicLong(0);
    private final AtomicLong waitingTimeTotal = new AtomicLong(0);

    public StationQueue(final Queue<T> queue) {
        this.queue = queue;
    }

    public Queue<T> getQueue() {
        return this.queue;
    }

    public int getMaxLength() {
        return maxLength.get();
    }

    public BigDecimal getWaitingTimeMaxInSeconds() {
        return BigDecimal.valueOf(waitingTimeMax.get()).divide(BigDecimal.valueOf(1000));
    }

    public BigDecimal getWaitingTimeAvgInSeconds() {
        if (numberOfProcessedElements.get() == 0) { // If the queue was never used
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(waitingTimeTotal.get() / numberOfProcessedElements.get()).divide(BigDecimal.valueOf(1000));
    }

    public int getNumberOfProcessedElements() {
        return numberOfProcessedElements.get();
    }

    public boolean offer(final T element) {
        if (maxLength.get() == 0 || queue.size() >= maxLength.get()) { // update the maximum size of this queue
            maxLength.set(queue.size() + 1);
        }
        return queue.offer(element);
    }

    public void process(final T element, final long processTime) {
        updateWaitingTimeMaximum(processTime - element.getArrivalTime());
        updateWaitingTimeTotal(processTime - element.getArrivalTime());
    }

    private void updateWaitingTimeMaximum(final long waitingTime) {
        if (waitingTime > waitingTimeMax.get()) {
            waitingTimeMax.set(waitingTime);
        }
    }

    private void updateWaitingTimeTotal(final long waitingTime) {
        numberOfProcessedElements.incrementAndGet();
        waitingTimeTotal.updateAndGet(current -> current + waitingTime);
    }
}

class Station<T extends Passenger> {

    private final String name;
    private final StationQueue<T> stationQueue;

    // Statistics which get updated as we process
    private final AtomicLong totalOccupancyTime = new AtomicLong(0);

    public Station(final String name, final StationQueue<T> stationQueue) {
        this.name = name;
        this.stationQueue = stationQueue;
    }

    public String getName() {
        return name;
    }

    public StationQueue<T> getStationQueue() {
        return stationQueue;
    }

    public Double getTotalOccupancyTimePercentage(final long processingDuration) {
        return Double.parseDouble(String.format("%.2f", ((double) totalOccupancyTime.get() / processingDuration) * 100));
    }

    public void updateTotalOccupancyTime(final long processingTime) {
        totalOccupancyTime.addAndGet(processingTime);
    }
}

class Passenger {

    private final long arrivalTime;

    public Passenger(final long arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public long getArrivalTime() {
        return arrivalTime;
    }
}
