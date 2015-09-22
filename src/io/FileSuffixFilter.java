package io;

import java.io.File;

import javax.swing.filechooser.FileFilter;


/**
 * 
 * @author Martin
 * Class that constructs a file filter that uses an input string as a suffix to test against
 */
public class FileSuffixFilter extends FileFilter implements Comparable<FileFilter> {
	
	String suffix;
	String descriptor;
	
	/**
	 * 
	 * @param suffix	- the file suffix for the filter to test
	 * @param descriptor	- descriptor string for filter
	 */
	public FileSuffixFilter (final String suffix, final String descriptor) {
		super ();
		this.suffix = suffix;
		this.descriptor = descriptor;
	}
	
	/**
	 * FileSuffixFilter specific method (i.e. not in FileFilter interface)
	 * @return String	the suffix
	 */
	public String getSuffix () { return suffix; }
	
	@Override
	public boolean accept (final File file) {
		return (file != null) && (file.isDirectory() || (file.isFile() && file.getName().endsWith (suffix)));
	}
			
	@Override
	public String getDescription() {
		return descriptor;
	}

	@Override
	public int compareTo (final FileFilter ffilter) {
		return getDescription().compareTo (ffilter.getDescription());
	}
	

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((descriptor == null) ? 0 : descriptor.hashCode());
		result = prime * result + ((suffix == null) ? 0 : suffix.hashCode());
		return result;
	}

	
	@Override
	public boolean equals (final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		
		final FileSuffixFilter other = (FileSuffixFilter) obj;
		if (descriptor == null) {
			if (other.descriptor != null) {
				return false;
			}
		} else if (!descriptor.equals(other.descriptor)) {
			return false;
		}
		
		if (suffix == null) {
			if (other.suffix != null) {
				return false;
			}
		} else if (!suffix.equals(other.suffix)) {
			return false;
		}
		
		return true;
	}
}