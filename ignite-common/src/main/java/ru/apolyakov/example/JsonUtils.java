package ru.apolyakov.example;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.introspect.AnnotatedClass;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.codec.binary.Base64;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;

import static java.util.stream.Collectors.toSet;

public class JsonUtils {
    public static boolean isNullOrWhiteSpace(final String s) {
        return s == null || s.trim().isEmpty();
    }

    public final static ObjectMapper mapper = new ObjectMapper();

    static {
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
    }

    public static byte[] convertObjectToJsonBytes(Object object) throws IOException {
        return mapper.writeValueAsBytes(object);
    }

    public static String convertObjectToJson(Object object) throws IOException {
        return mapper.writeValueAsString(object);
    }

    public static <C> C restoreFromJson(String objAsString, Class<C> clazz) throws IOException {
        return mapper.readValue(objAsString, clazz);
    }

    public static <C> C restoreFromObject(Object objAsString, Class<C> clazz) throws IOException {
        JsonNode jsonNode = mapper.valueToTree(objAsString);
        return restoreFromJson(jsonNode, clazz);
    }

    public static <C> C restoreFromJson(JsonNode jsonNode, Class<C> clazz) throws IOException {
        return mapper.treeToValue(jsonNode, clazz);
    }

    public static <C> C restoreTypedCollectionFromJson(JsonNode arrayNode, TypeReference<C> tTypeReference) throws IOException {
        ObjectReader reader = mapper.readerFor(tTypeReference);
        return reader.readValue(arrayNode);
    }

    public static JsonNode objectToJsonNode(Object o) {
        if (o == null)
            return null;
        return mapper.valueToTree(o);
    }

    public static JsonNode stringToJsonNode(String s) throws IOException {
        if (s == null)
            return null;
        return mapper.readTree(s);
    }

    public static <C> C restoreFromJson(InputStream inputStream, Class<C> clazz) throws IOException {
        return mapper.readValue(inputStream, clazz);
    }

    public static Set<String> listSubtypeNames(Class<?> cls) {
        DeserializationConfig deserializationConfig = mapper.getDeserializationConfig();
        JavaType javaType = deserializationConfig.constructType(cls);
        Collection<NamedType> types = mapper.getSubtypeResolver()
                .collectAndResolveSubtypesByClass(deserializationConfig, AnnotatedClass.construct(javaType, deserializationConfig));
        return types.stream()
                .map(NamedType::getName)
                .filter(Objects::nonNull)
                .collect(toSet());
    }

    public static String setWorkDirectory(IgniteConfiguration igniteConfig, @Value("${ignite.work-directory}") String workDirectory) {
        if (workDirectory == null || (!new File(workDirectory).isDirectory())) {
            Path path = FileSystems.getDefault().getPath("ignite/").toAbsolutePath();
            igniteConfig.setWorkDirectory(path.toString());
        } else {
            igniteConfig.setWorkDirectory(workDirectory);
        }
        return igniteConfig.getWorkDirectory();
    }


    public static String getVersion(final Class<?> source) {
        try {
            Object value = source.getPackage().getImplementationVersion();
            if (value != null && StringUtils.hasLength(value.toString())) {
                return value.toString();
            }
        } catch (Exception ex) {
            // Swallow and continue
        }
        return "dev";
    }

    public static String matToJson(Mat mat){
        JsonObject obj = new JsonObject();

        if(mat.isContinuous()){
            int cols = mat.cols();
            int rows = mat.rows();
            int elemSize = (int) mat.elemSize();
            int type = mat.type();

            obj.addProperty("rows", rows);
            obj.addProperty("cols", cols);
            obj.addProperty("type", type);

            // We cannot set binary data to a json object, so:
            // Encoding data byte array to Base64.
            String dataString;

            if( type == CvType.CV_32S || type == CvType.CV_32SC2 || type == CvType.CV_32SC3 || type == CvType.CV_16S) {
                int[] data = new int[cols * rows * elemSize];
                mat.get(0, 0, data);
                dataString = new String(Base64.encodeBase64(ConvertingUtils.toByteArray(data)));
            }
            else if( type == CvType.CV_32F || type == CvType.CV_32FC2) {
                float[] data = new float[cols * rows * elemSize];
                mat.get(0, 0, data);
                dataString = new String(Base64.encodeBase64(ConvertingUtils.toByteArray(data)));
            }
            else if( type == CvType.CV_64F || type == CvType.CV_64FC2) {
                double[] data = new double[cols * rows * elemSize];
                mat.get(0, 0, data);
                dataString = new String(Base64.encodeBase64(ConvertingUtils.toByteArray(data)));
            }
            else if( type == CvType.CV_8U ) {
                byte[] data = new byte[cols * rows * elemSize];
                mat.get(0, 0, data);
                dataString = new String(Base64.encodeBase64(data));
            }
            else {

                throw new UnsupportedOperationException("unknown type");
            }
            obj.addProperty("data", dataString);

            Gson gson = new Gson();
            String json = gson.toJson(obj);

            return json;
        } else {
            System.out.println("Mat not continuous.");
        }
        return "{}";
    }

    public static Mat matFromJson(String json){
        JsonObject JsonObject = JsonParser.parseString(json).getAsJsonObject();

        int rows = JsonObject.get("rows").getAsInt();
        int cols = JsonObject.get("cols").getAsInt();
        int type = JsonObject.get("type").getAsInt();

        Mat mat = new Mat(rows, cols, type);

        String dataString = JsonObject.get("data").getAsString();
        if( type == CvType.CV_32S || type == CvType.CV_32SC2 || type == CvType.CV_32SC3 || type == CvType.CV_16S) {
            int[] data = ConvertingUtils.toIntArray(Base64.decodeBase64(dataString.getBytes()));
            mat.put(0, 0, data);
        }
        else if( type == CvType.CV_32F || type == CvType.CV_32FC2) {
            float[] data = ConvertingUtils.toFloatArray(Base64.decodeBase64(dataString.getBytes()));
            mat.put(0, 0, data);
        }
        else if( type == CvType.CV_64F || type == CvType.CV_64FC2) {
            double[] data = ConvertingUtils.toDoubleArray(Base64.decodeBase64(dataString.getBytes()));
            mat.put(0, 0, data);
        }
        else if( type == CvType.CV_8U ) {
            byte[] data = Base64.decodeBase64(dataString.getBytes());
            mat.put(0, 0, data);
        }
        else {

            throw new UnsupportedOperationException("unknown type");
        }
        return mat;
    }
}
