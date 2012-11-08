package org.gramlab.gateway;

import java.io.File;
import java.util.List;

 public interface Gateway {
	 
	 void addEntry(File f);
	 void deleteEntry(String id);
	 void deleteEntries(List<String> ids);
	 

}
