package ru.pelengator.model;

import java.util.Arrays;
import java.util.Optional;



public class LinearRegression3 {

    private double beta;

    private double[] weights;

    private double learningRate = 0.001d;

    private int epochs;

    //private Function<T, R>

    public LinearRegression3(int featuresCount, int epochs) {
        weights = new double[featuresCount];
        this.epochs = epochs;
    }

    public Optional<Double> predict(double[] inputs) {
        if (inputs == null || inputs.length <= 0) {
            return Optional.empty();
        }

        double result = 0d;
        for (int i = 0; i < inputs.length; i++) {
            result = inputs[i] * weights[i] + result;
        }

        result = result + beta;

        return Optional.of(result);
    }



    public void trainSGD(double[][] trainData, double[] result) {

        if (trainData == null || trainData.length <= 0) {
            throw new RuntimeException("Input data can not be null");
        }
        // Stochastic Gradient descent
        for (int e = 0; e < epochs; e++) {
            double mse = 0d;
            for (int i = 0; i < trainData.length; i++) {
                double[] tempInput = trainData[i];

                Optional<Double> predictedValueOptional = this.predict(tempInput);

                double predictedValue = predictedValueOptional.get();

                double error = predictedValue - result[i];
                mse = error * error + mse;

                for (int j = 0; j < weights.length; j++) {
                    weights[j] = weights[j] - learningRate * error * tempInput[j];

                }
                beta = beta - learningRate * error;

            }

            mse = (Math.sqrt(mse)) / trainData.length;
            System.out.println(" MSE " + mse + " Weights " + Arrays.toString(weights) + " Beta " + beta);
        }

    }

    private static void trainModel()
    {
        double[][]   trainSet = {{25}, {25}, {25}, {25}, {25}, {50}, {50}, {50}, {50}, {50}}; // Consecutive hours developer codes
        double[] result ={4096, 3300, 2918, 5952, 3402, 10547, 3892,3651,827,10418}; // Number of bugs produced

    //    double[][] trainSet = {{20},{16},{19.8},{18.4},{17.1},{15.5}};
     //   double[] result = {88.6,71.6,93.3,84.3,80.6,75.2};
        LinearRegression3 linearRegression = new LinearRegression3(trainSet[0].length, 1000);
        linearRegression.trainSGD(trainSet, result);

    }

    public static void main(String[] args) {
        trainModel();
       // trainModel1();
        //testRandom();
    }
}
