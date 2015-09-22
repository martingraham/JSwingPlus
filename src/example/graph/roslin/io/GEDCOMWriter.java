package example.graph.roslin.io;

import io.DataPrep;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import model.graph.Edge;
import model.graph.GraphModel;
import model.graph.impl.DirectedGraphInstance;

import org.apache.log4j.Logger;

import util.XMLConstants2;

import example.graph.roslin.Animal;



public class GEDCOMWriter {

	final static Logger LOGGER = Logger.getLogger (GEDCOMWriter.class);
	protected transient OutputStream oStream;
	final static FileFilter TXT_FILTER = new FileFilter () {
		public boolean accept (final File pathname) {
			return pathname.isDirectory() || pathname.getName().endsWith (".txt");
		}
	};
	final static FileFilter GED_FILTER = new FileFilter () {
		public boolean accept (final File pathname) {
			return pathname.isDirectory() || pathname.getName().endsWith (".ged");
		}
	};
	
	public static final void main (final String[] args) {		
		final String inputFilename = args[0] == null ? "pedigree/FinnishF2Pedigree.txt" : args[0];
		convertThisFile (new File (inputFilename));	
		DataPrep.getInstance().makeZip ("GEDFormatPedigrees", new File (inputFilename), GED_FILTER);
	}
	
	public static void convertThisFile (final File file) {
		
		if (file.isDirectory()) {
			final File[] files = file.listFiles (TXT_FILTER);
			for (File dirFile : files) {
				convertThisFile (dirFile);
			}
		} else {
			LOGGER.info ("Converting file: "+file.getPath());
			final GraphModel graph = new DirectedGraphInstance ();

			try {
				final InputStream iStream = DataPrep.getInstance().getPossiblyZippedInputStream (file.getPath(), false);
				final PedigreeRoslinNodeReader prr = new PedigreeRoslinNodeReader (iStream);
				prr.populate (graph, Pattern.compile ("\t"));
				iStream.close();
			} catch (FileNotFoundException fnfe) {
				LOGGER.debug ("File not found for reading", fnfe);
			} catch (final IOException ioe) {
				LOGGER.error ("Can't close input stream", ioe);
			}
			
			if (graph.getNodeCount () > 5) {
				OutputStream oStream;
				try {
					oStream = new FileOutputStream (file.getPath()+".ged");
					final GEDCOMWriter gedcomWriter = new GEDCOMWriter (oStream);
					gedcomWriter.write (graph);
					oStream.flush();
					oStream.close();
				} catch (final FileNotFoundException fnfe) {
					LOGGER.error ("Cannot write file", fnfe);
				} catch (IOException ioe) {
					LOGGER.error ("Error flushing/closing output stream", ioe);
				}	
			}
		}
	}
	
	
	public GEDCOMWriter (final OutputStream oStream) {
		this.oStream = oStream;
	}
	
	public void write (final GraphModel graph) {
		
		final Collection<Object> nodes = graph.getNodes ();
		final Map<Object, Integer> idMap = new HashMap <Object, Integer> ();
		int id = 10000;
		for (Object node : nodes) {
			if (node instanceof Animal) {
				final Animal animal = (Animal)node;
				final Integer iid = Integer.valueOf (id);
				idMap.put (animal, iid);
				id++;
			}
		}
		
		
		final Set<Edge> edges = graph.getEdges ();
		final Map<Object, List<Object>> fatherMap = new HashMap<Object, List<Object>> ();
		//final Map<Object, List<Object>> motherMap = new HashMap<Object, List<Object>> ();
		final Map<Object, Object> childToMotherMap = new HashMap<Object, Object> ();
		
		// Make maps of fathers to children (1 : N) and children to mothers (1 : 1)
		for (Edge edge : edges) {
			final Object childNode = edge.getNode2();
			final Object parentNode = edge.getNode1();
			final Animal parent = (Animal)parentNode;
			if (parent.isMale()) {
				List<Object> children = fatherMap.get (parent);
				if (children == null) {
					children = new ArrayList<Object> ();
					fatherMap.put (parent, children);
				}
				children.add (childNode);
			} else {
				childToMotherMap.put (childNode, parentNode);
			}
		}
		
		
		
		final Map<Object, Map <Object, List<Object>>> fatherMotherMap =
			new HashMap<Object, Map <Object, List<Object>>> ();
		
		// Combines these maps into a cascaded map of father --> mother --> children
		for (Map.Entry<Object, List<Object>> fatherMapEntry : fatherMap.entrySet()) {
			final Object father = fatherMapEntry.getKey();
			final Map<Object, List<Object>> perFatherMotherChildMap = new HashMap<Object, List<Object>> ();
			fatherMotherMap.put (father, perFatherMotherChildMap);
			for (Object child : fatherMapEntry.getValue()) {
				final Object mother = childToMotherMap.get (child);
				List<Object> children = perFatherMotherChildMap.get (mother);
				if (children == null) {
					children = new ArrayList<Object> ();
					perFatherMotherChildMap.put (mother, children);
				}
				children.add (child);
			}
		}
		
		
		int fid = 50000;
		final List<Family> familyList = new ArrayList <Family> ();
		// Use this cascaded map to make a list of Family objects 
		for (Map.Entry<Object, Map <Object, List<Object>>> fatherMapEntry : fatherMotherMap.entrySet()) {
			final Object father = fatherMapEntry.getKey ();
			final Map <Object, List<Object>> perFatherMotherChildMap = fatherMapEntry.getValue();
			
			for (Map.Entry<Object, List<Object>> motherMapEntry : perFatherMotherChildMap.entrySet()) {
				final Object mother = motherMapEntry.getKey ();
				final List<Object> children = motherMapEntry.getValue();
				final Family family = new Family (father, mother, children, Integer.valueOf(fid));
				familyList.add (family);
				fid++;
			}
		}
		
		
		
		// Map individuals to families
		final Map<Object, List<Family>> childFamilyMap = new HashMap<Object, List<Family>> ();
		final Map<Object, List<Family>> spouseFamilyMap = new HashMap<Object, List<Family>> ();
		for (Family family : familyList) {
			final Object[] spouses = {family.getMale(), family.getFemale()};
			
			for (Object spouse : spouses) {
				List<Family> spouseFamilies = spouseFamilyMap.get (spouse);
				if (spouseFamilies == null) {
					spouseFamilies = new ArrayList<Family> ();
					spouseFamilyMap.put (spouse, spouseFamilies);
				}
				spouseFamilies.add (family);
			}
			
			for (Object child : family.getChildren()) {
				List<Family> childFamilies = childFamilyMap.get (child);
				if (childFamilies == null) {
					childFamilies = new ArrayList<Family> ();
					childFamilyMap.put (child, childFamilies);
				}
				childFamilies.add (family);
			}
		}
		
		
		PrintWriter pWriter;
		try {
			final SimpleDateFormat sdf = new SimpleDateFormat ("dd MMM yyyy");
			pWriter = DataPrep.getInstance().makeBufferedPrintWriter (oStream, XMLConstants2.UTF8, false);
			pWriter.println ("0 HEAD");
			pWriter.println ("1 SOUR MADE_UP");
			pWriter.println ("2 VERS 1.0");
			pWriter.println ("2 NAME JSWINGPLUS");
			pWriter.println ("2 CORP ROSLIN_NAPIER");
			pWriter.println ("3 ADDR EDINBURGH");
			pWriter.println ("4 CONT SCOTLAND");
			pWriter.println ("3 PHON +44 131 xxx xxxx");
			pWriter.println ("2 DATA ROSLIN");
			pWriter.println ("1 DEST QUILTS");
			pWriter.println ("1 DATE "+sdf.format(new Date()).toUpperCase());
			pWriter.println ("1 FILE C:\\whatever");
			pWriter.println ("1 GEDC");
			pWriter.println ("2 VERS 5.5");
			pWriter.println ("2 FORM LINEAGE-LINKED");
			pWriter.println ("1 CHAR UNICODE");
			pWriter.println ("1 LANG ENGLISH");
		
			for (Object node : nodes) {
				if (node instanceof Animal) {
					final Animal animal = (Animal)node;
					final Integer iid = idMap.get (animal);
					pWriter.println ("0 @I"+iid.toString()+"@ INDI");
					pWriter.println ("1 NAME "+animal.getName());
					pWriter.println ("1 SEX "+(animal.isMale() ? "M" : "F"));
					final List<Family> spouseFamilies = spouseFamilyMap.get (node);
					if (spouseFamilies != null) {
						for (Family family : spouseFamilies) {
							pWriter.println ("1 FAMS @F"+family.getFamilyID()+"@");
						}
					}
					final List<Family> childFamilies = childFamilyMap.get (node);
					if (childFamilies != null) {
						for (Family family : childFamilies) {
							pWriter.println ("1 FAMC @F"+family.getFamilyID()+"@");
						}
					}
				}
			}
			
			for (Family family : familyList) {
				pWriter.println ("0 @F"+family.getFamilyID()+"@ FAM");
				
				final Object male = family.getMale ();
				if (male != null) {
					pWriter.println ("1 HUSB @I"+idMap.get(male.toString())+"@");
				}
				
				final Object female = family.getMale ();
				if (female != null) {
					pWriter.println ("1 WIFE @I"+idMap.get(female).toString()+"@");
				}
				
				final List<Object> children = family.getChildren();
				if (children != null) {
					for (Object child : children) {
						pWriter.println ("1 CHIL @I"+idMap.get(child).toString()+"@");
					}
				}
			}
			
			pWriter.println ("0 TRLR");
			
			pWriter.flush();
			pWriter.close();
			
		} catch (IOException ioe) {
			LOGGER.error ("IOException in writing", ioe);
		}
	}
	
	
	
	/**
	 * Class object to represent GEDCOM's concept of family
	 * @author cs22
	 *
	 */
	static class Family {
		final Object male;
		final Object female;
		final List<Object> children;
		final int familyID;
		
		Family (final Object male, final Object female, final List<Object> children, final int familyID) {
			this.male = male;
			this.female = female;
			this.children = children;
			this.familyID = familyID;
		}
		
		public Object getMale() {
			return male;
		}

		public Object getFemale() {
			return female;
		}

		public List<Object> getChildren() {
			return children;
		}
		
		public int getFamilyID () { return familyID; }
		
		public void addChild (final Object child) {
			children.add (child);
		}
	}
}
