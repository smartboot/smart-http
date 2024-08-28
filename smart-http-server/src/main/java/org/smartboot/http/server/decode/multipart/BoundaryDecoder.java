//package org.smartboot.http.server.decode.multipart;
//
//import org.smartboot.http.common.Multipart;
//import org.smartboot.http.common.utils.Constant;
//import org.smartboot.http.server.HttpServerConfiguration;
//import org.smartboot.http.server.decode.AbstractDecoder;
//import org.smartboot.http.server.decode.Decoder;
//import org.smartboot.http.server.impl.Request;
//import java.nio.ByteBuffer;
//
///**
// * @Description: TODO
// * @Author MiSinG
// * @Date 2024/6/26
// * @Version V1.0
// **/
//public class BoundaryDecoder extends AbstractDecoder {
//
//    private static volatile BoundaryDecoder INSTANCE;
//
//    private BoundaryDecoder(HttpServerConfiguration configuration) {
//        super(configuration);
//    }
//
//    @Override
//    protected Decoder decode0(ByteBuffer byteBuffer, Request request) {
//
//        Multipart multipart = request.getMultipart();
//
//        int boundaryLength = multipart.getBoundaryLength() + Multipart.STREAM_TERMINATOR.length;
//        byte[] buffer = new byte[Multipart.FIELD_SEPARATOR.length];
//
//        if (request.getParts().isEmpty()) {
//            byteBuffer.position(byteBuffer.position() + boundaryLength);
//        }
//
//        byteBuffer.get(buffer);
//
//        boolean hasNextPart = isBoundaryValid(buffer);
//
//        multipart.setBufferPos(boundaryLength);
//
//        return hasNextPart ? PartHeaderDecoder.getInstance(getConfiguration()).decode(byteBuffer, request) : null;
//
//    }
//
//    private boolean isBoundaryValid(byte[] buffer) {
//        if (buffer[0] == Constant.CR && buffer[1] == Constant.LF) {
//            // 有下一段part
//            return true;
//        }
//
//        if (buffer[0] == Constant.DASH && buffer[1] == Constant.DASH) {
//            // 无下一段part
//            return false;
//        }
//
//        throw new IllegalArgumentException("Invalid boundary detected.");
//    }
//
//
//    public static String getBoundary(String contentType) {
//        String boundary = null;
//        if (contentType != null) {
//            int index = contentType.indexOf("boundary=");
//            if (index != -1) {
//                boundary = contentType.substring(index + 9);
//            }
//        }
//        return boundary;
//    }
//
//    public static BoundaryDecoder getInstance(HttpServerConfiguration configuration) {
//        if (INSTANCE == null) {
//            synchronized (BoundaryDecoder.class) {
//                if (INSTANCE == null) {
//                    INSTANCE = new BoundaryDecoder(configuration);
//                }
//            }
//        }
//        return INSTANCE;
//    }
//}
