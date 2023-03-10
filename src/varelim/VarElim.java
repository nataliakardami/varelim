package varelim;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.PriorityQueue;

public class VarElim {
    private Variable query;
    private ArrayList<ObsVar> observed;
    private ArrayList<Variable> variables;
    private ArrayList<Factor> factors;
    private PriorityQueue<Variable> queue;
    private int elimWidth;

     /**
      * Constructor for UI
      * @param ui
      * @param vars
      */
      public VarElim(UserInterface ui, ArrayList<Variable> vars){
        this.query = ui.getQueriedVariable();
        this.observed = ui.getObservedVariables();
        this.variables = vars;
        this.factors = makeFactors();
        this.queue = new PriorityQueue<>(vars);
        this.elimWidth =1;
    }
    /**
     * Constructor for manual input 
     * @param vars
     * @param query
     * @param observed
     */
    public VarElim(ArrayList<Variable> vars, Variable query, ArrayList<ObsVar> observed){
        this.query = query;
        this.observed = observed;
        this.variables = vars;
        this.factors = makeFactors();
        this.queue = new PriorityQueue<>(vars);
        this.elimWidth =1;

        
    }


        /*
    function:
        1) make factors
        2) reduce / restrict (take query variable and only use the portion that is relevant
         e.g a = young, before summing out)
        3) for all hidden variables eliminate H
          - multiply all factors that contain H -> factor g
          - sum out the variable H from factor g
        4) multiply and normalize remaining factors

     */

        /**
         * This function executes the variable elimination algorithm.
         */
        public void start(){
            ArrayList<Factor> factors = this.factors;

            //ArrayList<ObsVar> evidence = queue.poll();
            setElimWidth();
            System.out.println("Tree width: " +getElimWidth());
            
            System.out.println("Step 1: Reducing the factors by the observed variables: ");
            
            
            for(Factor f: factors){

                f = f.reduce(observed); // this operation reduces the existing factor, no need to delete or add.
               
            }
            // setElimWidth();
            System.out.println("Step 2: For each hidden variable: ");

            // Remove observed and evidence from queue to only have hidden variabled
            queue.remove(query);
            for (ObsVar obs:observed){
                queue.remove(obs.getVar());
            }
           
            // TODO last factor multiplied is always 0

            while (!queue.isEmpty()){  // FOR EACH HIDDEN VARIABLE IN QUEUE
                Variable elim = queue.poll();
                
                System.out.println("----------------------------eliminate "+elim);
                // ArrayList<Factor> toMult = new ArrayList<Factor>();
                ArrayList<Factor> toRemove = new ArrayList<Factor>();
                Factor newf = new Factor(1.0);
                // factors.remove(0);
                for(Factor f: factors){
                    
                    if (f.contains(elim)){ // if a factor f mentions the variable to be eliminated
                        setElimWidth();
                        Factor fc  = new Factor(f.multiply(newf)); // add factor to list of candidates   
                        System.out.println(f+" x "+newf);
                        newf = fc;
                        setElimWidth(newf.dimension()); // sets elim width to max after multiplication
                        toRemove.add(f); // this is a problem 
                    }
              
                   
                }
                // remove old factors from this.factors
                factors.removeAll(toRemove);

                // sum out the elim variable
                // and add the new factor to the factor list
                if (newf.contains(elim)){ // && !newf.contains(query)
                    Factor summed = new Factor(newf.sumOut(elim)); // TODO SUM OUT makes everything 0 on the last elimination
                    factors.add(summed);
                }
                else{
                    factors.add(newf);
                }
                // summing out john calls M,E 
                // and that makes all 0


            }
            System.out.println("Step 3: Multiply the remaining factors");

            // grab first factor
            ArrayList<Factor> toRemove2 = new ArrayList<Factor>();
            Factor first = factors.get(0);
           
            factors.remove(0);

            while(factors.size() >1){ // while theres 2 factors left

                for(Factor f: factors){
                    Factor fc  = new Factor(f.multiply(first)); // add factor to list of candidates   
                    System.out.println(f+" remov");
                    first = fc;
                    elimWidth = getElimWidth();
                    toRemove2.add(f);        
               }
               // remove old factors from this.factors
               elimWidth = getElimWidth();
               factors.removeAll(toRemove2);
             

            }
            
            



            System.out.println("Step 4: Normalize");
            // factors.get(0).normalize();
            System.out.println(factors);
            System.out.println("Elimination width of this run: "+elimWidth);



            
        }
    


    public Factor multiplyFactors(){
        while (!queue.isEmpty()){
            Variable elim = queue.poll();
            
            
            //ArrayList<Factor> toMult = factors;
            ArrayList<Factor> toRemove = new ArrayList<Factor>();
            Factor newf = factors.get(0);
            factors.remove(0);
            for(Factor f: factors){
                
                 if (f.contains(elim) ){ // if a factor f mentions the variable to be eliminated
                    Factor fc  = new Factor(f.multiply(newf)); // add factor to list of candidates   
                    System.out.println(f+" removed");
                    newf = fc;
                    toRemove.add(f);
                }
               
            }
            // remove old factors from this.factors
            factors.removeAll(toRemove);


        
    }
    return null;    
}

    /**
     * Compiles an Arraylist of all the initial factors
     * by iterating over each node of the network (besides the observed)
    */
    public  ArrayList<Factor> makeFactors(){
        ArrayList<Factor> f = new ArrayList<Factor>();
        //PriorityQueue ??
        //f.add(new Factor(query));
        for (Variable var:this.variables){
            f.add(new Factor(var));
        }
        return f;
    }

    public void reduceObserved(){
            for (Factor f:factors){
                f.reduce(observed);
            }

    }

    


     // ********** SETTERS AND GETTERS ************
    public int getElimWidth(){
        int maxdim = elimWidth;
        for (Factor f:factors){
            if (f.dimension()>maxdim)
                maxdim = f.dimension();
        }
        this.elimWidth = maxdim;
        return maxdim;
    }
    public void setElimWidth(){
        this.elimWidth = getElimWidth();
    }
    public void setElimWidth(int width){
        if(this.elimWidth<width){
            this.elimWidth = width;
        }
    }
    public ArrayList<Factor> getFactors() {
         return factors;
    }

    public ArrayList<ObsVar> getObserved(){
        return this.observed;
    }
}
