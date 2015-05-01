package crazypants.enderzoo.gen.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;

import crazypants.enderzoo.IoUtil;
import crazypants.enderzoo.Log;
import crazypants.enderzoo.gen.StructureRegister;
import crazypants.enderzoo.gen.structure.StructureTemplate;
import crazypants.enderzoo.gen.structure.StructureGenerator;

public class StructureResourceManager {

  public static final String CONFIG_EXT = ".cfg";
  public static final String TEMPLATE_EXT = ".nbt";
  
  private final List<ResourcePath> resourcePaths = new ArrayList<ResourcePath>();
  private final TemplateParser parser = new TemplateParser();
  private final StructureRegister register;
  
  public StructureResourceManager(StructureRegister register) {  
    this.register = register;       
  }

  public void addResourcePath(File dir) {
    resourcePaths.add(new ResourcePath(dir.getAbsolutePath(), true));
  }

  public void addResourcePath(String resourcePath) {
    resourcePaths.add(new ResourcePath(resourcePath, false));
  }
  
  public void addRuleFactory(IRuleFactory fact) {
    parser.getRuleFactory().add(fact);
  }

  public StructureGenerator loadTemplate(String uid) throws Exception {
    return parseJsonTemplate(loadTemplateText(uid));
  }
  
  public StructureGenerator parseJsonTemplate(String json) throws Exception {
    return parser.parseTemplate(register, json);
  }
  
  public String loadTemplateText(String uid) throws IOException {
    return IoUtil.readStream(getStreamForTemplate(uid));
  }

  public StructureTemplate loadStructureData(String uid) throws IOException {
    InputStream stream = null;
    try {
      stream = getStreamForStructureData(uid);
      if(stream == null) {
        throw new IOException("StructureResourceManager: Could find resources for template " + uid);        
      }
      return new StructureTemplate(stream);
    } finally {
      IOUtils.closeQuietly(stream);
    }
  }

  private InputStream getStreamForTemplate(String uid) {
    return getStream(uid + CONFIG_EXT);
  }

  private InputStream getStreamForStructureData(String uid) {
    return getStream(uid + TEMPLATE_EXT);
  }

  private InputStream getStream(String resourceName) {
    for (ResourcePath rp : resourcePaths) {
      InputStream is = rp.getStream(resourceName);
      if(is != null) {
        return is;
      }
    }
    return null;
  }

  private static class ResourcePath {

    private final String root;
    private final File dir;
    private final boolean isFile;

    public ResourcePath(String root, boolean isFile) {
      this.root = root;
      this.isFile = isFile;
      File tmp = null;
      if(isFile) {
        File f = new File(root);
        if(f.exists()) {
          tmp = f;
        }
      }
      dir = tmp;
    }

    public InputStream getStream(String name) {
      if(isFile) {
        if(dir == null) {
          return null;
        }
        try {
          return new FileInputStream(new File(dir, name));
        } catch (FileNotFoundException e) {
          return null;
        }
      } else {
        return StructureRegister.class.getResourceAsStream(root + name);
      }
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + (isFile ? 1231 : 1237);
      result = prime * result + ((root == null) ? 0 : root.hashCode());
      return result;
    }

    @Override
    public boolean equals(Object obj) {
      if(this == obj)
        return true;
      if(obj == null)
        return false;
      if(getClass() != obj.getClass())
        return false;
      ResourcePath other = (ResourcePath) obj;
      if(isFile != other.isFile)
        return false;
      if(root == null) {
        if(other.root != null)
          return false;
      } else if(!root.equals(other.root))
        return false;
      return true;
    }

  }
  
}
