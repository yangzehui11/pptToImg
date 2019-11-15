package ykd.ppt;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
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
import java.util.Map;

@CrossOrigin
@RestController
public class UploadController {
    private Qiniu qiniuCloudUtil=new Qiniu();

    @RequestMapping(method = RequestMethod.POST,
            value = "/upload")
    public Map upload(@RequestParam("file")MultipartFile file) throws IOException {
//application/octet-stream
        String houzui=file.getOriginalFilename().split("\\.")[1];
        FileInputStream fileImg = null;
        try {
            File f=File.createTempFile("tmp", null);
            file.transferTo(f);
            f.deleteOnExit();
            Map map=new HashMap<String,Object>();
            fileImg = new FileInputStream(f);
            if(houzui.equals("ppt")){
                doPPT2003toImage(fileImg,map);
            }else if (houzui.equals("pptx")){
                doPPT2007toImage(fileImg,map);
            }else if(houzui.equals("pdf")){
                PDFToImg(fileImg,map);
            } else  if(houzui.equals("jpg")||houzui.equals("jpeg")){
                String s=qiniuCloudUtil.upload(fileImg);
                ArrayList list=new ArrayList<String>();
                list.add(s);
                map.put("arr",list);
                /*map.put("windth",);
                map.put("height",pgsize.height);*/
            }
            return map;
        } catch (Exception e) {
            e.printStackTrace();
            return  null;
        }finally {
            fileImg.close();
        }

    }

    public boolean doPPT2003toImage(FileInputStream is, Map<String,Object> map) {
        try {
            ArrayList list=new ArrayList<String>();
            //is = new FileInputStream(pptFile);
            SlideShow ppt = new SlideShow(is);
            //及时关闭掉 输入流
            //is.close();
            Dimension pgsize = ppt.getPageSize();
            Slide[] slide = ppt.getSlides();

            for (int i = 0; i < slide.length; i++) {
                /*TextRun[] truns = slide[i].getTextRuns();
                for (int k = 0; k < truns.length; k++) {
                    RichTextRun[] rtruns = truns[k].getRichTextRuns();
                    for (int l = 0; l < rtruns.length; l++) {
                        // 重新设置 字体索引 和 字体名称 是为了防止生成的图片乱码问题
                        rtruns[l].setFontIndex(1);
                        rtruns[l].setFontName("宋体");
                    }
                }*/
                //根据幻灯片大小生成
                // 图片开始
                BufferedImage img = new BufferedImage(pgsize.width,pgsize.height, BufferedImage.TYPE_INT_RGB);
                //BufferedImage img = new BufferedImage(400,300, BufferedImage.TYPE_INT_RGB);
                //图片结束
                Graphics2D graphics = img.createGraphics();
                graphics.setPaint(Color.white);
                graphics.fill(new Rectangle2D.Float(0, 0, pgsize.width,pgsize.height));
                slide[i].draw(graphics);
                //BufferedImage转InputStream
                ByteArrayOutputStream os = null;
                InputStream input = null;
                try {
                    os = new ByteArrayOutputStream();
                    ImageIO.write(img, "png", os);
                    input = new ByteArrayInputStream(os.toByteArray());
                    String s=qiniuCloudUtil.upload(input);
                    list.add(s);
                } catch (IOException e) {
                    return false;
                } finally {
                    //关闭流
                    if (os != null) {
                        os.close();
                    }
                    if (input != null) {
                        input.close();
                    }

                }
            }
            map.put("arr",list);
            map.put("windth",pgsize.width);
            map.put("height",pgsize.height);
            return true;
        } catch (Exception e) {
            //log.error("PPT转换成图片 发生异常！", e);
            System.out.println("PPT转换成图片 发生异常！");
            return false;
        }
    }

    public Boolean doPPT2007toImage(FileInputStream is, Map<String,Object> map) {
        try {
            ArrayList list=new ArrayList<String>();
            XMLSlideShow xmlSlideShow = new XMLSlideShow(is);
            // 获取大小
            Dimension pgsize = xmlSlideShow.getPageSize();
            // 获取幻灯片
            XSLFSlide[] slides = xmlSlideShow.getSlides();
            for (int i = 0 ; i < slides.length ; i++) {
                // 解决乱码问题
                /*XSLFShape[] shapes = slides[i].getShapes();
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
                }*/
                //根据幻灯片大小生成图片
                BufferedImage img = new BufferedImage(pgsize.width,pgsize.height, BufferedImage.TYPE_INT_RGB);
                //BufferedImage img = new BufferedImage(400,300, BufferedImage.TYPE_INT_RGB);
                Graphics2D graphics = img.createGraphics();
                graphics.setPaint(Color.white);
                graphics.fill(new Rectangle2D.Float(0, 0, pgsize.width,pgsize.height));
                // 最核心的代码
                slides[i].draw(graphics);
                //BufferedImage转InputStream
                ByteArrayOutputStream os = null;
                InputStream input = null;
                try {
                    os = new ByteArrayOutputStream();
                    ImageIO.write(img, "png", os);
                    input = new ByteArrayInputStream(os.toByteArray());
                    String s=qiniuCloudUtil.upload(input);
                    list.add(s);
                } catch (IOException e) {
                    return false;
                } finally {
                    if (os != null) {
                        os.close();
                    }
                    if (input != null) {
                        input.close();
                    }
                }
            }
            map.put("arr",list);
            map.put("windth",pgsize.width);
            map.put("height",pgsize.height);
            return true;
        }
        catch (Exception e) {
        }
        return false;
    }

    /**
     * PDF转图片 当前是根据页码一页一页转，需要改成一次转完
     * imgType:转换后的图片类型 jpg,png
     */
    //          os,pdf文件流,上传到的map集合
    public boolean PDFToImg( FileInputStream is, Map<String,Object> map) throws IOException {
        PDDocument pdDocument = null;
        /* dpi越大转换后越清晰，相对转换速度越慢 */
        int dpi = 100;
        try {
            ArrayList list=new ArrayList<String>();
            pdDocument=PDDocument.load(is);
            PDFRenderer renderer = new PDFRenderer(pdDocument);
            int pages = pdDocument.getNumberOfPages();
            BufferedImage image=null;
            for (int i = 0; i <pages ; i++) {
                //BufferedImage转InputStream
                ByteArrayOutputStream os = null;
                InputStream input = null;
                try {
                    image = renderer.renderImageWithDPI(i,dpi);
                    os = new ByteArrayOutputStream();
                    ImageIO.write(image, "png", os);
                    input = new ByteArrayInputStream(os.toByteArray());
                    String s=qiniuCloudUtil.upload(input);
                    list.add(s);

                } catch (IOException e) {
                    return false;
                } finally {
                    //关闭流
                    if (os != null) {
                        os.close();
                    }
                    if (input != null) {
                        input.close();
                    }
                }
            }
            map.put("arr",list);
            map.put("windth",image.getWidth());
            map.put("height",image.getHeight());
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            if (pdDocument != null) {
                pdDocument.close();
            }
        }

    }




}