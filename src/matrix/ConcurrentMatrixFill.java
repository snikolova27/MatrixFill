package matrix;

import utils.Utils;

import java.util.Calendar;
import java.util.Random;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

public class ConcurrentMatrixFill {

    private int[][] matrix;
    private final int countOfThreads;
    private int[] numsToFillWith;

    public ConcurrentMatrixFill(int countOfThreads) {
        this.countOfThreads = countOfThreads;
        this.matrix = new int[this.countOfThreads][Utils.MAX_COLS];
        generateNewNumbers();
    }

    public String execute() throws InterruptedException {
        StringBuilder finalResult = new StringBuilder();
        finalResult.append(printGeneratedRandomNumbers());

        // за измерване на изминалото време за паралелното изпълнение
        long startParallel, finishParallel;
        startParallel = Calendar.getInstance().getTimeInMillis();

        // паралелно запълване
        finalResult.append(concurrentFill());
        finishParallel = Calendar.getInstance().getTimeInMillis();
        finalResult.append("Time for parallel execution in milliseconds: ").append(finishParallel - startParallel).append("\n");
        finalResult.append("Matrix: \n");
        finalResult.append(printMatrix());
        finalResult.append("=================================================================================\n");
        finalResult.append("\n");

        // за изминалото време за последователното изпълнение
        long startLinear, finishLinear;
        startLinear = Calendar.getInstance().getTimeInMillis();

        // генерираме нови числа, с които да попълним матрицата последователно
        generateNewNumbers();
        finalResult.append(printGeneratedRandomNumbers());

        // последователното изпълнение
        finalResult.append(linearFill());
        finishLinear = Calendar.getInstance().getTimeInMillis();
        finalResult.append("Time for linear execution in milliseconds: ").append(finishLinear - startLinear).append("\n");
        finalResult.append("Matrix: \n");
        finalResult.append(printMatrix());
        finalResult.append("=================================================================================\n");

        //  System.out.println(finalResult);
        return String.valueOf(finalResult);
    }

    private String printRow(int row) {
        StringBuilder sb = new StringBuilder();
        sb.append("Row ");
        sb.append(row);
        sb.append("\t");
        for (int i = 0; i < Utils.MAX_COLS; i++) {
            sb.append(this.matrix[row][i]);
            if (i + 1 < Utils.MAX_COLS) {
                sb.append(", ");
            }
        }
        sb.append("\n");
        return String.valueOf(sb);
    }

    public String printMatrix() {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < this.countOfThreads; i++) {
            result.append(printRow(i));
        }
        //  System.out.println(result);
        return String.valueOf(result);
    }

    private String concurrentFillRow(int row) {
        StringBuilder sb = new StringBuilder();
        long threadId = Thread.currentThread().getId();
        sb.append("Currently filling row " + row + " by thread with id " + threadId + " with number " + this.numsToFillWith[row] + "\n");

        fillRow(row);
        // System.out.println(sb);
        return String.valueOf(sb);
    }

    private void fillRow(int row) {
        // итерираме по колоните на съответния ред и запълваме с числото, което сме генерирали за тази нишка
        for (int i = 0; i < Utils.MAX_COLS; i++) {
            this.matrix[row][i] = this.numsToFillWith[row];
        }
    }

    public String concurrentFill() throws InterruptedException {
        StringBuilder sb = new StringBuilder();
        sb.append("Concurrent fill\n");
        sb.append("==========================\n");

        // използваме AtomicInteger, за да се предпазим от проблеми свързани с паметта и нейната консистентност
        AtomicInteger rowIdx = new AtomicInteger(0);
        ExecutorService executorService = Executors.newFixedThreadPool(this.countOfThreads);

        for (int i = 0; i < this.countOfThreads; i++) {
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    // всяка нишка запълва един ред с генерирано за нея число в интервала 0 - 177
                    sb.append(concurrentFillRow(rowIdx.getAndIncrement()));
                }
            });
        }

        executorService.shutdown();
        executorService.awaitTermination(1, TimeUnit.MINUTES);
        sb.append("Matrix fill done. \n");

        // System.out.println(sb);
        return String.valueOf(sb);
    }

    public String linearFill() {
        StringBuilder sb = new StringBuilder();
        sb.append("Linear fill \n");
        sb.append("==========================\n");
        for (int i = 0; i < this.countOfThreads; i++) {
            fillRow(i);
        }
        sb.append("Matrix fill done. \n");
        // System.out.println(sb);
        return String.valueOf(sb);
    }

    private void generateNewNumbers() {
        this.numsToFillWith = IntStream.generate(() -> new Random().nextInt(90) + 27).limit(this.countOfThreads).toArray();
    }

    public String printGeneratedRandomNumbers() {
        StringBuilder sb = new StringBuilder();
        sb.append("The generated numbers to fill the matrix with: ");
        for (int i = 0; i < this.countOfThreads; i++) {
            sb.append(this.numsToFillWith[i]);
            sb.append(" ");
        }
        sb.append("\n");
        return String.valueOf(sb);
    }
}
