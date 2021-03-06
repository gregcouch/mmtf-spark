/**
 * 
 */
package edu.sdsc.mmtf.spark.io.demos;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.rcsb.mmtf.api.StructureDataInterface;

import edu.sdsc.mmtf.spark.filters.ExperimentalMethods;
import edu.sdsc.mmtf.spark.filters.Resolution;
import edu.sdsc.mmtf.spark.filters.Rfree;
import edu.sdsc.mmtf.spark.io.MmtfReader;
import edu.sdsc.mmtf.spark.io.MmtfWriter;

/**
 * @author peter
 *
 */
public class WriteMmtfCustom {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		String path = System.getProperty("MMTF_FULL");
	    if (path == null) {
	    	    System.err.println("Environment variable for Hadoop sequence file has not been set");
	        System.exit(-1);
	    }
	    
	    long start = System.nanoTime();
	    
	    SparkConf conf = new SparkConf().setMaster("local[*]").setAppName(WriteMmtfCustom.class.getSimpleName());
	    JavaSparkContext sc = new JavaSparkContext(conf);
		 
	    // read a 20% random sample of the PDB
	    double fraction = 0.2;
	    long seed = 123;
	    JavaPairRDD<String, StructureDataInterface> pdb = MmtfReader.readSequenceFile(path, fraction, seed, sc);

	    // retain high resolution X-ray structures
	    pdb = pdb
	    		.filter(new ExperimentalMethods(ExperimentalMethods.X_RAY_DIFFRACTION))
	    		.filter(new Resolution(0, 2.0))
	    		.filter(new Rfree(0, 0.2));
    
	    // save this subset in a Hadoop Sequence file
	    pdb = pdb.coalesce(8);
	    MmtfWriter.writeSequenceFile(path +"_xray", sc, pdb);
	    
	    System.out.println("# structures: " + pdb.count());
	  
	    long end = System.nanoTime();
	    
	    System.out.println("Time: " + (end-start)/1E9 + "sec.");
	    
	    sc.close();
	}
}
