package com.inadco.hbl.compiler;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterOutputStream;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.Validate;
import org.apache.hadoop.conf.Configuration;
import org.yaml.snakeyaml.TypeDescription;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;

import com.inadco.hbl.api.Cube;
import com.inadco.hbl.model.HexDimension;
import com.inadco.hbl.model.SimpleCube;
import com.inadco.hbl.model.SimpleCuboid;
import com.inadco.hbl.model.SimpleMeasure;
import com.inadco.hbl.model.SimpleTimeHierarchy;

/**
 * parse cube mode definition out of Yaml document.
 * 
 * (see example module for examples of cube definitions).
 * 
 * @author dmitriy
 * 
 */

public final class YamlModelParser {

    public static Cube parseYamlModel(InputStream is) throws IOException {

        Yaml y = getYaml();
        return (Cube) y.load(new InputStreamReader(is, "utf-8"));
    }

    public static Cube parseYamlModel(String str) throws IOException {

        Yaml y = getYaml();
        return (Cube) y.load(str);
    }

    public static String toYaml(Cube c) {
        Yaml y = getYaml();
        return y.dump(c);
    }

    public static Cube getCubeModel(Configuration conf) throws IOException {
        String modelStr = conf.get(Pig8CubeIncrementalCompilerBean.PROP_CUBEMODEL);
        if (modelStr == null)
            return null;
        return decodeCubeModel(modelStr);
    }
    
    public static Cube decodeCubeModel ( String encoded ) throws IOException { 
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        OutputStream os = new InflaterOutputStream(baos,new Inflater(false));
        os.write(Base64.decodeBase64(encoded.getBytes("US-ASCII")));
        os.close();
        return parseYamlModel(new String(baos.toByteArray(),"utf-8"));
    }
    
    public static String encodeCubeModel ( String yamlStr ) throws IOException { 
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Writer w = new OutputStreamWriter(new DeflaterOutputStream(baos,new Deflater(9, false)),"utf-8");
        w.append(yamlStr);
        w.close();
        
        String result = new String(Base64.encodeBase64(baos.toByteArray()), "US-ASCII");
        decodeCubeModel(result);
        return result;
    }

    public static void initCubeModel(String yamlStr, Configuration confTo) throws IOException {
        Validate.notNull(yamlStr);
        Validate.notNull(confTo);
        confTo.set(Pig8CubeIncrementalCompilerBean.PROP_CUBEMODEL,
                   encodeCubeModel(yamlStr));
    }

    private static Yaml getYaml() {
        Constructor c = new Constructor(SimpleCube.class);
        Representer rp = new Representer();

        // understand !SimpleCuboid as custom tag
        addTag(SimpleCuboid.class, c, rp);
        addTag(HexDimension.class, c, rp);
        addTag(SimpleTimeHierarchy.class, c, rp);
        addTag(SimpleMeasure.class, c, rp);
        return new Yaml(c, rp);

    }

    private static void addTag(Class<?> cl, Constructor c, Representer r) {
        Tag t = new Tag("!" + cl.getSimpleName());
        c.addTypeDescription(new TypeDescription(cl, t));
        r.addClassTag(cl, t);
    }

}