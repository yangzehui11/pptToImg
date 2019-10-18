package ykd.ppt;

import org.apache.poi.hslf.model.Slide;
import org.apache.poi.hslf.model.TextRun;
import org.apache.poi.hslf.usermodel.RichTextRun;
import org.apache.poi.hslf.usermodel.SlideShow;
import org.apache.poi.xslf.usermodel.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
@CrossOrigin
@RestController
public class UploadController {
    private Qiniu qiniuCloudUtil=new Qiniu();

    @RequestMapping(method = RequestMethod.POST,
            value = "/upload")
    public List upload(@RequestParam("file")MultipartFile file){
//application/octet-stream
        String houzui=file.getOriginalFilename().split("\\.")[1];
        System.out.println(houzui);
        try {
            File f=File.createTempFile("tmp", null);
            file.transferTo(f);
            f.deleteOnExit();
            ArrayList<String> arr = new ArrayList<>();
            if(houzui.equals("ppt")){
                doPPT2003toImage(f,arr);
            }else if (houzui.equals("pptx")){
                doPPT2007toImage(f,arr);
            }
            return new HashMap<String,List>().put("arr",arr);
        } catch (Exception e) {
            e.printStackTrace();
            return  null;
        }

    }

    public boolean doPPT2003toImage(File pptFile, List<String> list) {
        try {
            FileInputStream is = new FileInputStream(pptFile);
            SlideShow ppt = new SlideShow(is);
            //及时关闭掉 输入流
            is.close();
            Dimension pgsize = ppt.getPageSize();
            Slide[] slide = ppt.getSlides();

            for (int i = 0; i < slide.length; i++) {
                TextRun[] truns = slide[i].getTextRuns();
                for (int k = 0; k < truns.length; k++) {
                    RichTextRun[] rtruns = truns[k].getRichTextRuns();
                    for (int l = 0; l < rtruns.length; l++) {
                        // 重新设置 字体索引 和 字体名称 是为了防止生成的图片乱码问题
                        rtruns[l].setFontIndex(1);
                        rtruns[l].setFontName("宋体");
                    }
                }
                //根据幻灯片大小生成
                // 图片开始
                BufferedImage img = new BufferedImage(pgsize.width,pgsize.height, BufferedImage.TYPE_INT_RGB);
                //图片结束
                Graphics2D graphics = img.createGraphics();
                graphics.setPaint(Color.white);
                graphics.fill(new Rectangle2D.Float(0, 0, pgsize.width,pgsize.height));
                slide[i].draw(graphics);
                //BufferedImage转InputStream
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                try {
                    ImageIO.write(img, "png", os);
                    InputStream input = new ByteArrayInputStream(os.toByteArray());
                    String s=qiniuCloudUtil.upload(input);
                    list.add(s);
                } catch (IOException e) {
                    return false;
                }
            }
            return true;
        } catch (Exception e) {
            //log.error("PPT转换成图片 发生异常！", e);
            System.out.println("PPT转换成图片 发生异常！");
            return false;
        }
    }

    public Boolean doPPT2007toImage(File pptFile,List<String> list) {
        FileInputStream is = null ;
        try {
            is = new FileInputStream(pptFile);
            XMLSlideShow xmlSlideShow = new XMLSlideShow(is);
            is.close();
            // 获取大小
            Dimension pgsize = xmlSlideShow.getPageSize();
            // 获取幻灯片
            XSLFSlide[] slides = xmlSlideShow.getSlides();
            for (int i = 0 ; i < slides.length ; i++) {
                // 解决乱码问题
                XSLFShape[] shapes = slides[i].getShapes();
                for (XSLFShape shape : shapes) {
                    if (shape instanceof XSLFTextShape) {
                        XSLFTextShape sh = (XSLFTextShape) shape;
                        List<XSLFTextParagraph> textParagraphs = sh.getTextParagraphs();
                        for (XSLFTextParagraph xslfTextParagraph : textParagraphs) {
                            List<XSLFTextRun> textRuns = xslfTextParagraph.getTextRuns();
                            for (XSLFTextRun xslfTextRun : textRuns) {
                                xslfTextRun.setFontFamily("宋体");
                            }
                        }
                    }
                }
                //根据幻灯片大小生成图片
                BufferedImage img = new BufferedImage(pgsize.width,pgsize.height, BufferedImage.TYPE_INT_RGB);
                Graphics2D graphics = img.createGraphics();
                graphics.setPaint(Color.white);
                graphics.fill(new Rectangle2D.Float(0, 0, pgsize.width,pgsize.height));
                // 最核心的代码
                slides[i].draw(graphics);
                //BufferedImage转InputStream
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                try {
                    ImageIO.write(img, "png", os);
                    InputStream input = new ByteArrayInputStream(os.toByteArray());
                    String s=qiniuCloudUtil.upload(input);
                    list.add(s);
                } catch (IOException e) {
                    return false;
                }

            }
            return true;
        }
        catch (Exception e) {
        }
        return false;
    }


}