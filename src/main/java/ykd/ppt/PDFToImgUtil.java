package ykd.ppt;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.Map;

public class PDFToImgUtil {

    private static Logger logger = LoggerFactory.getLogger(PDFToImgUtil.class);


    /**
     * 获取PDF总页数
     * @throws IOException
     */
    public static int getPDFNum(String fileUrl) throws IOException {
        PDDocument pdDocument = null;
        int pages = 0;
        try {
            //pdDocument = getPDDocument(fileUrl);
            pages = pdDocument.getNumberOfPages();
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e.getMessage(),e);
        } finally {
            if (pdDocument != null) {
                pdDocument.close();
            }
        }
        return pages;
    }


    /**
     * PDF转图片 当前是根据页码一页一页转，需要改成一次转完
     * imgType:转换后的图片类型 jpg,png
     */
                                //          os,pdf文件,页码，上传到的map集合
    public boolean PDFToImg(OutputStream sos, FileInputStream is, Map<String,Object> map) throws IOException {
        PDDocument pdDocument = null;
        /* dpi越大转换后越清晰，相对转换速度越慢 */
        int dpi = 100;
        try {
            ArrayList list=new ArrayList<String>();
            PDDocument.load(is);
            PDFRenderer renderer = new PDFRenderer(pdDocument);
            int pages = pdDocument.getNumberOfPages();
            BufferedImage image=null;
            for (int i = 0; i <=pages ; i++) {
                //BufferedImage转InputStream
                ByteArrayOutputStream os = null;
                InputStream input = null;
                try {
                    image = renderer.renderImageWithDPI(i,dpi);
                    os = new ByteArrayOutputStream();
                    ImageIO.write(image, "png", sos);
                    input = new ByteArrayInputStream(os.toByteArray());
                    //String s=qiniuCloudUtil.upload(input);
                    //list.add(s);

                } catch (IOException e) {
                    return false;
                } finally {
                    //关闭流
                    os.close();
                    input.close();
                }
            }
            map.put("arr",list);
            map.put("windth",image.getWidth());
            map.put("height",image.getHeight());
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e.getMessage(),e);
            return false;
        } finally {
            if (pdDocument != null) {
                pdDocument.close();
            }
        }

    }


    /*private static PDDocument getPDDocument(String fileUrl) throws IOException {
        File file = new File(fileUrl);
        FileInputStream inputStream = new FileInputStream(file);
        return PDDocument.load(inputStream);
    }*/

}