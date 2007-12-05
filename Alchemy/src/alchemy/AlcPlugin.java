/**
 * AlcPlugin.java
 *
 * Created on November 22, 2007, 6:38 PM
 *
 * @author  Karl D.D. Willis
 * @version 1.0
 */

package alchemy;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Iterator;

import java.util.ArrayList;

import java.net.URI;
import java.net.URL;

// JAVA PLUGIN FRAMEWORK
import org.java.plugin.ObjectFactory;
import org.java.plugin.PluginManager;
import org.java.plugin.PluginManager.PluginLocation;
import org.java.plugin.registry.Extension;
import org.java.plugin.registry.ExtensionPoint;
import org.java.plugin.registry.PluginDescriptor;
import org.java.plugin.standard.StandardPluginLocation;

public class AlcPlugin implements AlcConstants{
    
    // PLUGIN
    private PluginManager pluginManager;
    
    private int numberOfPlugins;
    private int numberOfCreateModules = 0;
    private int numberOfAffectModules = 0;
    
    
    AlcMain root;
    
    /** Creates a new instance of AlcPlugin */
    public AlcPlugin(AlcMain root) {
        
        this.root = root;
        
        pluginManager = ObjectFactory.newInstance().createManager();
        
        // Folder of the plugins
        File pluginsDir = new File("plugins");
        
        // Get all plugins that end with .zip
        File[] plugins = pluginsDir.listFiles(new FilenameFilter() {
            
            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith(".zip");
            }
            
        });
        
        try {
            
            PluginLocation[] locations = new PluginLocation[plugins.length];
            
            // Number of plugins minus one for the core plugin
            numberOfPlugins = plugins.length-1;
            int numberOfCreates = 0;
            int numberOfAffects = 0;
            
            for (int i = 0; i < plugins.length; i++) {
                locations[i] = StandardPluginLocation.create(plugins[i]);
                
                // Check for each type of module using the filename
                if(plugins[i].getName().startsWith("alchemy.create")){
                    numberOfCreateModules++;
                } else if(plugins[i].getName().startsWith("alchemy.affect")){
                    numberOfAffectModules++;
                }
                //System.out.println(plugins[i].getName());
            }
            
            // Registers plug-ins and their locations with this plug-in manager.
            pluginManager.publishPlugins(locations);
            
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        
        
    }
    
    public AlcModule[] addPlugins(String pointName, int numberOfModules, int moduleType){
        
        AlcModule[] plugins = new AlcModule[numberOfModules];
        int index = 0;
        
        try{
            PluginDescriptor core = pluginManager.getRegistry().getPluginDescriptor("alchemy.core");
            
            ExtensionPoint point = pluginManager.getRegistry().getExtensionPoint(core.getId(), pointName);
            
            for (Iterator<Extension> it = point.getConnectedExtensions().iterator(); it.hasNext();) {
                
                Extension ext = it.next();
                PluginDescriptor descr = ext.getDeclaringPluginDescriptor();
                pluginManager.activatePlugin(descr.getId());
                
                ClassLoader classLoader = pluginManager.getPluginClassLoader(descr);
                Class pluginCls = classLoader.loadClass(ext.getParameter("class").valueAsString());
                
                plugins[index] = ( (AlcModule)pluginCls.newInstance() );
                AlcModule currentPlugin = plugins[index];
                
                // Set the icon name and the decription name from the XML
                String descriptionParam = ext.getParameter("description").valueAsString();
                String iconParam = ext.getParameter("icon").valueAsString();
                String nameParam = ext.getParameter("name").valueAsString();
                
                URL iconUrl = null;
                
                if (iconParam != null) {
                    iconUrl = classLoader.getResource(iconParam);
                    currentPlugin.setIconUrl(iconUrl);
                }
                
                // TODO - How to load .class files from here?
                
                
                // Set the root value
                currentPlugin.setRoot(root);
                currentPlugin.setModuleType(moduleType);
                currentPlugin.setName(nameParam);
                currentPlugin.setIconName(iconParam);
                currentPlugin.setDescription(descriptionParam);
                currentPlugin.setIndex(index);
                
                index++;
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return plugins;
    }
    
    public int getNumberOfPlugins(){
        return numberOfPlugins;
    }
    
    public int getNumberOfCreateModules(){
        return numberOfCreateModules;
    }
    
    public int getNumberOfAffectModules(){
        return numberOfAffectModules;
    }
    
}