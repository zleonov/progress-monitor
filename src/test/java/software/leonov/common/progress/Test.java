package software.leonov.common.progress;

public class Test {

    public static void main(String[] args) {
        ProgressMonitor progress = ProgressMonitor.withConstantStepSize(100);
        
        //progress.setMaximum(1000);
        
        progress.addProgressListener(event -> System.out.println("Processed " + event.getProgress() + " events"));
        
        progress.setProgress(200);
        progress.setProgress(233);
        
        for(int i= 0; i < 1000; i++)
            progress.increment();
        
        progress.completed();
        
        
        
        progress.completed();
        progress.completed();
        progress.completed();

    }

}
