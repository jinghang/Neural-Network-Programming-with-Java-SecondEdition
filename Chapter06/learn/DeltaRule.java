
package edu.packt.neuralnet.learn;

import edu.packt.neuralnet.HiddenLayer;
import edu.packt.neuralnet.NeuralException;
import edu.packt.neuralnet.NeuralNet;
import edu.packt.neuralnet.Neuron;
import edu.packt.neuralnet.OutputLayer;
import edu.packt.neuralnet.chart.Chart;
import edu.packt.neuralnet.data.NeuralDataSet;
import java.util.ArrayList;
import org.jfree.chart.ChartFrame;

/**
 * This class implements Delta Rule algorithm. 
 * 
 * @author Alan de Souza, Fabio Soares
 * @version 0.1
 *
 */
public class DeltaRule extends LearningAlgorithm {
    
    public ArrayList<ArrayList<Double>> error;
    public ArrayList<Double> generalError;
    public ArrayList<Double> overallError;
    public double overallGeneralError;
    
    public ArrayList<ArrayList<Double>> testingError;
    public ArrayList<Double> testingGeneralError;
    public ArrayList<Double> testingOverallError;
    public double testingOverallGeneralError;
    
    
    public double degreeGeneralError=2.0;
    public double degreeOverallError=0.0;
    
    public enum ErrorMeasurement {SimpleError, SquareError,NDegreeError,MSE}
    
    public ErrorMeasurement generalErrorMeasurement=ErrorMeasurement.SquareError;
    public ErrorMeasurement overallErrorMeasurement=ErrorMeasurement.MSE;
    
    protected int currentRecord=0;
    
    protected ArrayList<ArrayList<ArrayList<Double>>> newWeights;
    
    public DeltaRule(NeuralNet _neuralNet){
        this.learningParadigm=LearningParadigm.SUPERVISED;
        this.neuralNet=_neuralNet;
        this.newWeights=new ArrayList<>();
        int numberOfHiddenLayers=this.neuralNet.getNumberOfHiddenLayers();
        for(int l=0;l<=numberOfHiddenLayers;l++){
            int numberOfNeuronsInLayer,numberOfInputsInNeuron;
            this.newWeights.add(new ArrayList<ArrayList<Double>>());
            if(l<numberOfHiddenLayers){
                numberOfNeuronsInLayer=this.neuralNet.getHiddenLayer(l)
                        .getNumberOfNeuronsInLayer();
                for(int j=0;j<numberOfNeuronsInLayer;j++){
                    numberOfInputsInNeuron=this.neuralNet.getHiddenLayer(l)
                            .getNeuron(j).getNumberOfInputs();
                    this.newWeights.get(l).add(new ArrayList<Double>());
                    for(int i=0;i<=numberOfInputsInNeuron;i++){
                        this.newWeights.get(l).get(j).add(0.0);
                    }
                }
            }
            else{
                numberOfNeuronsInLayer=this.neuralNet.getOutputLayer()
                        .getNumberOfNeuronsInLayer();
                for(int j=0;j<numberOfNeuronsInLayer;j++){
                    numberOfInputsInNeuron=this.neuralNet.getOutputLayer()
                            .getNeuron(j).getNumberOfInputs();
                    this.newWeights.get(l).add(new ArrayList<Double>());
                    for(int i=0;i<=numberOfInputsInNeuron;i++){
                        this.newWeights.get(l).get(j).add(0.0);
                    }
                }
            }
        }
    }
    
    public DeltaRule(NeuralNet _neuralNet,NeuralDataSet _trainDataSet){
        this(_neuralNet);
        this.trainingDataSet=_trainDataSet;
        if(_trainDataSet.inputNorm!=null)
            this.normalization=true;
        this.generalError=new ArrayList<>();
        this.error=new ArrayList<>();
        this.overallError=new ArrayList<>();
        for(int i=0;i<_trainDataSet.numberOfRecords;i++){
            this.generalError.add(null);
            this.error.add(new ArrayList<Double>());
            for(int j=0;j<_neuralNet.getNumberOfOutputs();j++){
                if(i==0){
                    this.overallError.add(null);
                }
                this.error.get(i).add(null);
            }
        }
    }
    
    public DeltaRule(NeuralNet _neuralNet,NeuralDataSet _trainDataSet
            ,LearningMode _learningMode){
        this(_neuralNet,_trainDataSet);
        this.learningMode=_learningMode;
    }
    
    @Override
    public void setTestingDataSet(NeuralDataSet _testingDataSet){
        this.testingDataSet=_testingDataSet;
        if(_testingDataSet.inputNorm!=null)
            this.normalization=true;
        this.testingGeneralError=new ArrayList<>();
        this.testingError=new ArrayList<>();
        this.testingOverallError=new ArrayList<>();
        for(int i=0;i<_testingDataSet.numberOfRecords;i++){
            this.testingGeneralError.add(null);
            this.testingError.add(new ArrayList<Double>());
            for(int j=0;j<this.neuralNet.getNumberOfOutputs();j++){
                if(i==0){
                    this.testingOverallError.add(null);
                }
                this.testingError.get(i).add(null);
            }
        }        
    }
    
    public void setGeneralErrorMeasurement(ErrorMeasurement _errorMeasurement){
        switch(_errorMeasurement){
            case SimpleError:
                this.degreeGeneralError=1;
                break;
            case SquareError:
            case MSE:
                this.degreeGeneralError=2;
        }
        this.generalErrorMeasurement=_errorMeasurement;
    }
    
    public void setOverallErrorMeasurement(ErrorMeasurement _errorMeasurement){
        switch(_errorMeasurement){
            case SimpleError:
                this.degreeOverallError=1;
                break;
            case SquareError:
            case MSE:
                this.degreeOverallError=2;
        }
        this.overallErrorMeasurement=_errorMeasurement;
    }
    
    @Override
    public Double calcNewWeight(int layer,int input,int neuron) 
            throws NeuralException{
        if(layer>0){
            throw new NeuralException("Delta rule can be used only with single"
                    + " layer neural network");
        }
        else{
            Double deltaWeight=LearningRate;
            Neuron currNeuron=neuralNet.getOutputLayer().getNeuron(neuron);
            switch(learningMode){
                case BATCH:
                    ArrayList<Double> derivativeResult=
                        currNeuron
                            .derivativeBatch(trainingDataSet
                                    .getArrayInputData(normalization));
                    ArrayList<Double> _ithInput;
                    if(input<currNeuron.getNumberOfInputs()){
                        _ithInput=trainingDataSet.getIthInputArrayList(input,normalization);
                    }
                    else{
                        _ithInput=new ArrayList<>();
                        for(int i=0;i<trainingDataSet.numberOfRecords;i++){
                            _ithInput.add(1.0);
                        }
                    }
                    Double multDerivResultIthInput=0.0;
                    for(int i=0;i<trainingDataSet.numberOfRecords;i++){
                        multDerivResultIthInput+=error.get(i).get(neuron)*
                                derivativeResult.get(i)*_ithInput.get(i);
                    }
                    deltaWeight*=multDerivResultIthInput;
                    break;
                case ONLINE:
                    deltaWeight*=error.get(currentRecord).get(neuron);
                    deltaWeight*=currNeuron
                                    .derivative(neuralNet.getInputs());
                    if(input<currNeuron.getNumberOfInputs()){
                        deltaWeight*=neuralNet.getInput(input);
                    }
                    break;
            }

            return currNeuron.getWeight(input)+deltaWeight;
        }
    }
    
    @Override
    public Double calcNewWeight(int layer,int input,int neuron,double error) 
            throws NeuralException{
        if(layer>0){
            throw new NeuralException("Delta rule can be used only with single"
                    + " layer neural network");
        }
        else{
            Double deltaWeight=LearningRate*error;
            Neuron currNeuron=neuralNet.getOutputLayer().getNeuron(neuron);
            switch(learningMode){
                case BATCH:
                    ArrayList<Double> derivativeResult=
                        currNeuron
                            .derivativeBatch(trainingDataSet
                                    .getArrayInputData(normalization));
                    ArrayList<Double> _ithInput;
                    if(input<currNeuron.getNumberOfInputs()){
                        _ithInput=trainingDataSet.getIthInputArrayList(input,normalization);
                    }
                    else{
                        _ithInput=new ArrayList<>();
                        for(int i=0;i<trainingDataSet.numberOfRecords;i++){
                            _ithInput.add(1.0);
                        }
                    }
                    Double multDerivResultIthInput=0.0;
                    for(int i=0;i<trainingDataSet.numberOfRecords;i++){
                        multDerivResultIthInput+=derivativeResult.get(i)*_ithInput.get(i);
                    }
                    deltaWeight*=multDerivResultIthInput;
                    break;
                case ONLINE:
                    deltaWeight*=currNeuron
                                    .derivative(neuralNet.getInputs());
                    if(input<currNeuron.getNumberOfInputs()){
                        deltaWeight*=neuralNet.getInput(input);
                    }
                    break;
            }

            return currNeuron.getWeight(input)+deltaWeight;
        }
    }    
    
    @Override
    public void train() throws NeuralException{
        if(neuralNet.getNumberOfHiddenLayers()>0){
            throw new NeuralException("Delta rule can be used only with single layer neural network");
        }
        else{
            switch(learningMode){
                case BATCH:
                    epoch=0;
                    forward();
                    if(printTraining){
                        print();
                    }                    
                    while(epoch<MaxEpochs && overallGeneralError>MinOverallError){
                        epoch++;                        
                        for(int j=0;j<neuralNet.getNumberOfOutputs();j++){
                            for(int i=0;i<=neuralNet.getNumberOfInputs();i++){
                                //weightUpdate(0, i, j,overallError.get(j));
                                newWeights.get(0).get(j).set(i, calcNewWeight(0,i,j));
                            }
                        }
                        applyNewWeights();
                        forward();
                        if(printTraining){
                            print();
                        }                        
                    }
                    break;
                case ONLINE:
                    epoch=0;
                    int k=0;
                    currentRecord=0;
                    forward(k);
                    if(printTraining){
                        print();
                    }                    
                    while(epoch<MaxEpochs && overallGeneralError>MinOverallError){
                        for(int j=0;j<neuralNet.getNumberOfOutputs();j++){
                            for(int i=0;i<=neuralNet.getNumberOfInputs();i++){
                                //weightUpdate(0, i, j,error.get(currentRecord)
                                //        .get(j));
                                newWeights.get(0).get(j).set(i,calcNewWeight(0,i,j));
                            }
                        }   
                        applyNewWeights();
                        currentRecord=++k;
                        if(k>=trainingDataSet.numberOfRecords){
                            k=0;
                            currentRecord=0;
                            epoch++;
                        }
                        
                        forward(k);
                        if(printTraining){
                            print();
                        }                        
                    }
                    
                    break;
                
            }
        }
    }
    
    public void applyNewWeights(){
        int numberOfHiddenLayers=this.neuralNet.getNumberOfHiddenLayers();
        for(int l=0;l<=numberOfHiddenLayers;l++){
            int numberOfNeuronsInLayer,numberOfInputsInNeuron;
            if(l<numberOfHiddenLayers){
                HiddenLayer hl = this.neuralNet.getHiddenLayer(l);
                numberOfNeuronsInLayer=hl.getNumberOfNeuronsInLayer();
                for(int j=0;j<numberOfNeuronsInLayer;j++){
                    numberOfInputsInNeuron=hl.getNeuron(j).getNumberOfInputs();
                    for(int i=0;i<=numberOfInputsInNeuron;i++){
                        double newWeight=this.newWeights.get(l).get(j).get(i);
                        hl.getNeuron(j).updateWeight(i, newWeight);
                    }
                }
            }
            else{
                OutputLayer ol = this.neuralNet.getOutputLayer();
                numberOfNeuronsInLayer=ol.getNumberOfNeuronsInLayer();
                for(int j=0;j<numberOfNeuronsInLayer;j++){
                    numberOfInputsInNeuron=ol.getNeuron(j).getNumberOfInputs();
                    
                    for(int i=0;i<=numberOfInputsInNeuron;i++){
                        double newWeight=this.newWeights.get(l).get(j).get(i);
                        ol.getNeuron(j).updateWeight(i, newWeight);
                    }
                }
            }
        }        
    }
    
    @Override
    public void forward(int i) throws NeuralException{
        if(neuralNet.getNumberOfHiddenLayers()>0){
            throw new NeuralException("Delta rule can be used only with single "
                    + "layer neural network");
        }
        else{
            neuralNet.setInputs(trainingDataSet.getArrayInputRecord(i,normalization));
            neuralNet.calc();
            trainingDataSet.setNeuralOutput(i, neuralNet.getOutputs(),normalization);
            generalError.set(i, 
                    generalError(
                            trainingDataSet.getArrayTargetOutputRecord(i,normalization)
                            ,trainingDataSet.getArrayNeuralOutputRecord(i,normalization)));
            for(int j=0;j<neuralNet.getNumberOfOutputs();j++){
                overallError.set(j, 
                        overallError(trainingDataSet
                                .getIthTargetOutputArrayList(j,normalization)
                                , trainingDataSet
                                        .getIthNeuralOutputArrayList(j,normalization)));
                error.get(i).set(j
                        ,simpleError(trainingDataSet
                                .getIthTargetOutputArrayList(j,normalization).get(i)
                                , trainingDataSet.getIthNeuralOutputArrayList(j,normalization)
                                        .get(i)));
            }
            overallGeneralError=overallGeneralErrorArrayList(
                    trainingDataSet.getArrayTargetOutputData(normalization)
                    ,trainingDataSet.getArrayNeuralOutputData(normalization));
            //simpleError=simpleErrorEach.get(i);
        }
    }
    
    @Override 
    public void forward() throws NeuralException{
        if(neuralNet.getNumberOfHiddenLayers()>0){
            throw new NeuralException("Delta rule can be used only with single"
                    + " layer neural network");
        }
        else{
            for(int i=0;i<trainingDataSet.numberOfRecords;i++){
                neuralNet.setInputs(trainingDataSet.getInputRecord(i,normalization));
                neuralNet.calc();
                trainingDataSet.setNeuralOutput(i, neuralNet.getOutputs(),normalization);
                generalError.set(i, 
                    generalError(
                            trainingDataSet.getArrayTargetOutputRecord(i,normalization)
                            ,trainingDataSet.getArrayNeuralOutputRecord(i,normalization)));
                for(int j=0;j<neuralNet.getNumberOfOutputs();j++){
                    error.get(i).set(j
                        ,simpleError(trainingDataSet
                                .getArrayTargetOutputRecord(i,normalization).get(j)
                                , trainingDataSet.getArrayNeuralOutputRecord(i,normalization)
                                        .get(j)));
                }
            }
            for(int j=0;j<neuralNet.getNumberOfOutputs();j++){
                overallError.set(j, 
                        overallError(trainingDataSet
                                .getIthTargetOutputArrayList(j,normalization)
                                , trainingDataSet
                                        .getIthNeuralOutputArrayList(j,normalization)));
            }
            overallGeneralError=overallGeneralErrorArrayList(
                    trainingDataSet.getArrayTargetOutputData(normalization)
                    ,trainingDataSet.getArrayNeuralOutputData(normalization));
            //simpleError=simpleErrorEach.get(trainingDataSet.numberOfRecords-1);
        }
    }
    
    @Override
    public void test(int i) throws NeuralException{
        if(neuralNet.getNumberOfHiddenLayers()>0){
            throw new NeuralException("Delta rule can be used only with single "
                    + "layer neural network");
        }
        else{
            neuralNet.setInputs(testingDataSet.getArrayInputRecord(i,normalization));
            neuralNet.calc();
            testingDataSet.setNeuralOutput(i, neuralNet.getOutputs(),normalization);
            testingGeneralError.set(i, 
                    generalError(
                            testingDataSet.getArrayTargetOutputRecord(i,normalization)
                            ,testingDataSet.getArrayNeuralOutputRecord(i,normalization)));
            for(int j=0;j<neuralNet.getNumberOfOutputs();j++){
                testingOverallError.set(j, 
                        overallError(testingDataSet
                                .getIthTargetOutputArrayList(j,normalization)
                                , testingDataSet
                                        .getIthNeuralOutputArrayList(j,normalization)));
                testingError.get(i).set(j
                        ,simpleError(testingDataSet
                                .getIthTargetOutputArrayList(j,normalization).get(i)
                                , testingDataSet.getIthNeuralOutputArrayList(j,normalization)
                                        .get(i)));
            }
            testingOverallGeneralError=overallGeneralErrorArrayList(
                    testingDataSet.getArrayTargetOutputData(normalization)
                    ,testingDataSet.getArrayNeuralOutputData(normalization));
            //simpleError=simpleErrorEach.get(i);
        }
    }
    
    @Override 
    public void test() throws NeuralException{
        if(neuralNet.getNumberOfHiddenLayers()>0){
            throw new NeuralException("Delta rule can be used only with single"
                    + " layer neural network");
        }
        else{
            for(int i=0;i<testingDataSet.numberOfRecords;i++){
                neuralNet.setInputs(testingDataSet.getInputRecord(i,normalization));
                neuralNet.calc();
                testingDataSet.setNeuralOutput(i, neuralNet.getOutputs(),normalization);
                testingGeneralError.set(i, 
                    generalError(
                            testingDataSet.getArrayTargetOutputRecord(i,normalization)
                            ,testingDataSet.getArrayNeuralOutputRecord(i,normalization)));
                for(int j=0;j<neuralNet.getNumberOfOutputs();j++){
                    testingError.get(i).set(j
                        ,simpleError(testingDataSet
                                .getArrayTargetOutputRecord(i,normalization).get(j)
                                , testingDataSet.getArrayNeuralOutputRecord(i,normalization)
                                        .get(j)));
                }
            }
            for(int j=0;j<neuralNet.getNumberOfOutputs();j++){
                testingOverallError.set(j, 
                        overallError(testingDataSet
                                .getIthTargetOutputArrayList(j,normalization)
                                , testingDataSet
                                        .getIthNeuralOutputArrayList(j,normalization)));
            }
            testingOverallGeneralError=overallGeneralErrorArrayList(
                    testingDataSet.getArrayTargetOutputData(normalization)
                    ,testingDataSet.getArrayNeuralOutputData(normalization));
            //simpleError=simpleErrorEach.get(trainingDataSet.numberOfRecords-1);
        }
    }
    
    
    public double overallGeneralError(Double[][] YT,Double[][] Y){
        int N=YT.length;
        int Ny=YT[0].length;
        Double result=0.0;
        for(int i=0;i<N;i++){
            Double resultY = 0.0;
            for(int j=0;j<Ny;j++){
                resultY+=Math.pow(YT[i][j]-Y[i][j], degreeGeneralError);
            }
            if(generalErrorMeasurement==ErrorMeasurement.MSE)
                result+=Math.pow((1.0/Ny)*resultY,degreeOverallError);
            else
                result+=Math.pow((1.0/degreeGeneralError)*resultY,degreeOverallError);
        }
        return (1.0/N)*result;
    }
    
    public Double overallGeneralErrorArrayList(ArrayList<ArrayList<Double>> YT,ArrayList<ArrayList<Double>> Y){
        int N=YT.size();
        int Ny=YT.get(0).size();
        Double result=0.0;
        for(int i=0;i<N;i++){
            Double resultY = 0.0;
            for(int j=0;j<Ny;j++){
                resultY+=Math.pow(YT.get(i).get(j)-Y.get(i).get(j), degreeGeneralError);
            }
            if(generalErrorMeasurement==ErrorMeasurement.MSE)
                result+=Math.pow((1.0/Ny)*resultY,degreeOverallError);
            else
                result+=Math.pow((1.0/degreeGeneralError)*resultY,degreeOverallError);
        }
        if(overallErrorMeasurement==ErrorMeasurement.MSE)
            result*=(1.0/N);
        else
            result*=(1.0/degreeOverallError);
        return result;
    }
    
    public Double generalError(ArrayList<Double> YT,ArrayList<Double> Y){
        int Ny=YT.size();
        Double result=0.0;
        for(int i=0;i<Ny;i++){
            result+=Math.pow(YT.get(i)-Y.get(i), degreeGeneralError);
        }
        if(generalErrorMeasurement==ErrorMeasurement.MSE)
            result*=(1.0/Ny);
        else
            result*=(1.0/degreeGeneralError);
        return result;
    }
    
    public Double overallError(ArrayList<Double> YT,ArrayList<Double> Y){
        int N=YT.size();
        Double result=0.0;
        for(int i=0;i<N;i++){
            result+=Math.pow(YT.get(i)-Y.get(i), degreeOverallError);
        }
        if(overallErrorMeasurement==ErrorMeasurement.MSE)
            result*=(1.0/N);
        else
            result*=(1.0/degreeOverallError);
        return result;
    }
    
    public Double generalError(Double[] YT,Double[] Y){
        int Ny=YT.length;
        Double result=0.0;
        for(int i=0;i<Ny;i++){
            result+=Math.pow(YT[i]-Y[i], degreeGeneralError);
        }
        if(generalErrorMeasurement==ErrorMeasurement.MSE)
            result*=(1.0/Ny);
        else
            result*=(1.0/degreeGeneralError);
        return result;        
    }
    
    public Double overallError(Double[] YT,Double[] Y){
        int N=YT.length;
        Double result=0.0;
        for(int i=0;i<N;i++){
            result+=Math.pow(YT[i]-Y[i], degreeOverallError);
        }
        if(overallErrorMeasurement==ErrorMeasurement.MSE)
            result*=(1.0/N);
        else
            result*=(1.0/degreeOverallError);
        return result;        
    }
   
    public Double simpleError(Double YT,Double Y){
        return YT-Y;
    }
    
    public Double squareError(Double YT,Double Y){
        return (1.0/2.0)*Math.pow(YT-Y,2.0);
    }
    
    
    @Override 
    public void print(){
        String msgPrint ="";
        if(learningMode==LearningMode.ONLINE)
            msgPrint ="Epoch="+String.valueOf(epoch)+"; Record="
                    +String.valueOf(currentRecord)+"; Overall Error="
                    +String.valueOf(overallGeneralError);
        else
            msgPrint="Epoch= "+String.valueOf(epoch)
                    +"; Overall Error ="+String.valueOf(overallGeneralError);
        if(testingDataSet!=null)
            msgPrint=msgPrint+"; Test Error="+String.valueOf(testingOverallGeneralError);
        System.out.println(msgPrint);
    }
    
    @Override
    public void showErrorEvolution(){
        
//        Chart chart = new Chart("Training",rndDataSet,seriesNames,0,seriesColor);
//            ChartFrame frame = new ChartFrame("Training", chart.scatterPlot("X", "Y"));
//            frame.pack();
//            frame.setVisible(true);
    }
    
    @Override
    public void showFittingEvolution(){
        
    }

    @Override
    public void showScatterFittingEvolution(){
        
    }
    
    public Double getOverallGeneralError(){
        return overallGeneralError;
    }
    
    public Double getOverallError(int output){
        return overallError.get(output);
    }
    
    public Double getTestingOverallGeneralError(){
        return testingOverallGeneralError;
    }
    
    public Double getTestingOverallError(int output){
        return testingOverallError.get(output);
    }
}
