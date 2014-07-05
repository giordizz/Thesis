/*
 *    This program is free software; you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 2 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this program; if not, write to the Free Software
 *    Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */

import java.io.FileReader;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import mulan.classifier.MultiLabelOutput;
import mulan.classifier.meta.RAkEL;
import mulan.classifier.neural.MMPLearner;
import mulan.classifier.transformation.LabelPowerset;
import mulan.data.InvalidDataFormatException;
import mulan.data.MultiLabelInstances;
import mulan.evaluation.Evaluation;
import mulan.evaluation.Evaluator;
import mulan.evaluation.MultipleEvaluation;
import mulan.examples.GettingPredictionsOnUnlabeledData;
import weka.classifiers.Classifier;
import weka.classifiers.trees.J48;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Utils;

/**
 * This examples shows how you can retrieve the predictions of a model on
 * unlabeled data. Unlabeled multi-label datasets should have the same
 * structure as the training data. The actual values of the labels could be
 * either unspecified (set to symbol ?), or randomly set to 0/1.
 *
 * @author Grigorios Tsoumakas
 * @version 2010.12.15
 */
public class MulanClassifier {

    /**
     * Executes this example
     *
     * @param args command-line arguments -arff, -xml and -unlabeled
     */
    public static void main(String[] args) {

        try {
            String arffFilename = Utils.getOption("arff", args);
            String xmlFilename = Utils.getOption("xml", args);
            System.out.println("Loading the training data set...");
            MultiLabelInstances trainingData = new MultiLabelInstances(arffFilename, xmlFilename);

//            RAkEL model = new RAkEL( new MMPLearner());
            RAkEL model = new RAkEL( new LabelPowerset(new J48()));
            System.out.println("Building the model...");
            model.build(trainingData);
            model.setSizeOfSubset(2);
            System.out.println("model built...");
            
          //  MultiLabelInstances trainingData = new MultiLabelInstances(arffFilename, xmlFilename);
            Evaluator eval = new Evaluator();
//            eval.evaluate(model, trainingData);
            
          
            Evaluation results;
           
            
            String testFilename = Utils.getOption("test", args);
            
            FileReader reader = new FileReader(testFilename);
            Instances testData = new Instances(reader);
            
            
            int numFolds = 10;
            
            results=eval.evaluate(model, trainingData);   
//            results = eval.crossValidate(model, trainingData, numFolds);
            System.out.println(results);
         
//            eval.evaluate(model, trainingData);
//            
           // System.out.println(numInstances);
//            Instances trData=trainingData.getDataSet();
//            int numInstances = trData.numInstances();
////            
//            for (Instance i : testData)
//            	System.out.println(i);
//            	
            	
            	
           /*     Instance instance = i;
                MultiLabelOutput output;
                if (instance == null)
                	System.out.println("err");
                else{
                	output = model.makePrediction(i);
                	System.out.println(output);
                }
              
                // do necessary operations with provided prediction output, here just print it out
               
            } */
//            results = eval.crossValidate(model, testData, numFolds);
//            System.out.println(results);
//            String unlabeledDataFilename = Utils.getOption("unlabeled", args);
//            System.out.println("Loading the unlabeled data set...");
//            MultiLabelInstances unlabeledData = new MultiLabelInstances(unlabeledDataFilename, xmlFilename);
//
//            int numInstances = unlabeledData.getNumInstances();
//            for (int instanceIndex = 0; instanceIndex < numInstances; instanceIndex++) {
//                Instance instance = unlabeledData.getDataSet().instance(instanceIndex);
//                MultiLabelOutput output = model.makePrediction(instance);
//                if (output.hasBipartition()) {
//                    String bipartion = Arrays.toString(output.getBipartition());
//                    System.out.println("Predicted bipartion: " + bipartion);
//                }
//            }
        } catch (InvalidDataFormatException e) {
            System.err.println(e.getMessage());
        } catch (Exception ex) {
            Logger.getLogger(GettingPredictionsOnUnlabeledData.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}