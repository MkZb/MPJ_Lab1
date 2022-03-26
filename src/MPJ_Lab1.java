import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

public class MPJ_Lab1 {
    static int N = 400; //Matrix and array size.
    static int P = 4; //Threads count. Set it so N is multiple of P.
    //Only read data
    static float[][] MD;
    static float[][] MT;
    static float[][] MZ;
    static float[] B;
    static float[] D;

    //Write data
    static float a = 0;
    static float[][] MTZ = new float[N][N];
    static float[][] MA = new float[N][N];
    static float[] E = new float[N];

    public static void main(String[] args) {
        long start = System.currentTimeMillis();

        Mon1 mon1 = new Mon1();
        Mon2 mon2 = new Mon2();
        Mon3 mon3 = new Mon3();
        Mon4 mon4 = new Mon4();

        System.out.println("Program started");
        Data data = new Data(N);
        data.loadData("test2.txt");
        MD = data.parseMatrix(N);
        MT = data.parseMatrix(N);
        MZ = data.parseMatrix(N);
        B = data.parseVector(N);
        D = data.parseVector(N);
        System.out.println("Data successfully parsed");

        for (int i = 0; i < P; i++) {
            int pNum = i;
            new Thread(new Runnable() {
                public void run() {
                    float maxMD = 0;
                    float[][] MTZpart = new float[N][N / P];
                    float[][] MTDpart = new float[N][N / P];
                    float[][] MApart = new float[N][N / P];
                    float[] Epart = new float[N / P];

                    System.out.println("Thread " + pNum + " started");
                    //Calc max(MD)
                    for (int j = 0; j < N; j++) {
                        for (int k = (N / P) * pNum; k < (N / P) * (pNum + 1); k++) {
                            if (MD[j][k] > maxMD) maxMD = MD[j][k];
                        }
                    }
                    setMax(maxMD);
                    mon1.signal_a();

                    //Calc B*MD+D*MT
                    for (int j = (N / P) * pNum; j < (N / P) * (pNum + 1); j++) {
                        float[] arrayToAdd = new float[2 * N];
                        for (int k = 0; k < N; k++) {
                            arrayToAdd[k] += B[k] * MD[j][k];
                            arrayToAdd[k + N] += D[k] * MT[j][k];
                        }
                        Arrays.sort(arrayToAdd);
                        for (int k = 0; k < 2 * N; k++) {
                            Epart[j - (N / P) * pNum] += arrayToAdd[k];
                        }
                    }
                    setE(Epart, pNum);
                    mon4.signal_calc();

                    mon1.wait_a();
                    //Calc max(MD)*(MT+MZ)
                    for (int j = 0; j < N; j++) {
                        for (int k = (N / P) * pNum; k < (N / P) * (pNum + 1); k++) {
                            MTZpart[j][k - (N / P) * pNum] = a * (MT[j][k] + MZ[j][k]);
                        }
                    }
                    setMTZ(MTZpart, pNum);
                    mon2.signal_calc();
                    mon2.wait_calc();
                    //Calc max(MD)*(MT+MZ)-MT*MD
                    for (int j = 0; j < N; j++) {
                        for (int k = (N / P) * (pNum); k < (N / P) * (pNum + 1); k++) {
                            float[] arrayToAdd = new float[N];
                            for (int l = 0; l < N; l++) {
                                arrayToAdd[l] = MT[j][l] * MD[l][k];
                            }
                            Arrays.sort(arrayToAdd);
                            MTDpart[j][k - (N / P) * (pNum)] = 0;
                            for (int l = 0; l < N; l++) {
                                MTDpart[j][k - (N / P) * (pNum)] += arrayToAdd[l];
                            }
                            MApart[j][k - (N / P) * (pNum)] = MTZ[j][k] - MTDpart[j][k - (N / P) * (pNum)];
                        }
                    }
                    setMA(MApart, pNum);
                    mon3.signal_calc();
                    mon3.wait_calc();
                    mon4.wait_calc();
                    if (pNum == 0) {
                        try {
                            long finish = System.currentTimeMillis();
                            long timeExecuted = finish - start;
                            File resultMA = new File("resultMA.txt");
                            File resultE = new File("resultE.txt");
                            FileWriter writer1 = new FileWriter("resultMA.txt");
                            FileWriter writer2 = new FileWriter("resultE.txt");
                            for (int j = 0; j < N; j++) {
                                for (int k = 0; k < N; k++) {
                                    System.out.print(MA[j][k] + " ");
                                    writer1.write(MA[j][k] + "\n");
                                }
                                System.out.println();
                            }

                            for (int j = 0; j < N; j++) {
                                System.out.print(E[j] + " ");
                                writer2.write(E[j] + "\n");
                            }
                            System.out.println();
                            writer1.close();
                            writer2.close();
                            System.out.println("Data successfully saved on disk");
                            System.out.println(timeExecuted + " milliseconds spent on calculations");
                        } catch (IOException e) {
                            System.out.println("An error occurred.");
                            e.printStackTrace();
                        }
                    }
                }

                public synchronized void setMax(float val) {
                    if (val > a) a = val;
                }

                public synchronized void setMTZ(float[][] val, int pNum) {
                    for (int i = 0; i < N; i++) {
                        for (int j = (N / P) * pNum; j < (N / P) * (pNum + 1); j++) {
                            MTZ[i][j] = val[i][j - (N / P) * pNum];
                        }
                    }
                }

                public synchronized void setMA(float[][] val, int pNum) {
                    for (int i = 0; i < N; i++) {
                        for (int j = (N / P) * pNum; j < (N / P) * (pNum + 1); j++) {
                            MA[i][j] = val[i][j - (N / P) * pNum];
                        }
                    }
                }

                public synchronized void setE(float[] val, int pNum) {
                    for (int i = (N / P) * pNum; i < (N / P) * (pNum + 1); i++) {
                        E[i] = val[i - (N / P) * pNum];
                    }
                }

            }).start();
        }
    }
}


