package workNote;


import java.io.*;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class FileHelper {

    public static boolean existFile(String p_filepath) {
        File tmpFile = new File(p_filepath);
        if (tmpFile.exists()) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean deleteFileIfExists(String p_filepath) {
        File tmpFile = new File(p_filepath);
        if (tmpFile.exists()) {
            tmpFile.delete();
            return true;
        } else {
            return false;
        }
    }


    public static boolean delDir(String dirPath) {
        File tmpFile = new File(dirPath);
        if (tmpFile.exists()) {
            if (tmpFile.isFile()) {
                if (tmpFile.delete()) {
                    return true;
                }
            } else if( tmpFile.isDirectory()) {
                String[] lists = tmpFile.list();
                if (lists == null) {
                    if (tmpFile.delete()) {
                        return true;
                    }
                } else {
                    for (String list : lists) {
                        if (!delDir(dirPath + File.separator + list)) {
                            break;
                        }
                    }
                }
            }
        }
        return false;
    }


    public static byte[] readFileBytes(String p_filepath) {
        File tmpFile = new File(p_filepath);
        if (!tmpFile.exists()) {
            return null;
        }
        Long filelength = tmpFile.length();
        byte[] filecontent = new byte[filelength.intValue()];
        try {
            FileInputStream in = new FileInputStream(tmpFile);
            in.read(filecontent);
            in.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return filecontent;
    }

    /**
     * 将byte数组写入文件
     *
     * @param path
     * @param fileName
     * @param content
     * @throws IOException
     */
    public static void writeFile(String path, String fileName, byte[] content)
            throws IOException {
        try {
            File f = new File(path);
            if (!f.exists()) {
                f.mkdirs();
            }
            FileOutputStream fos = new FileOutputStream(path + fileName);
            fos.write(content);
            fos.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    /**
     * 将byte数组写入文件
     *
     * @param filePath
     * @param content
     * @throws IOException
     */
    public static void writeBytesToFile(String filePath, byte[] content)
            throws IOException {
    	FileOutputStream fos = null;
        try {
            File f = new File(filePath);
            File paf = f.getParentFile();
            if (paf !=null && !paf.exists()) {
            	paf.mkdirs();
            }
            fos = new FileOutputStream(f);
            fos.write(content);
            fos.close();
            fos = null;
        } catch (IOException e) {
            throw e;
        }finally{
        	if(fos != null){
        		fos.close();
        	}
        }
    }
    
    public static String readFileText(String p_filepath, String p_encoding) {
        File tmpFile = new File(p_filepath);
        if (!tmpFile.exists()) {
            return null;
        }
        Long filelength = tmpFile.length();
        byte[] filecontent = new byte[filelength.intValue()];
        try {
            FileInputStream in = new FileInputStream(tmpFile);
            in.read(filecontent);
            in.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            return new String(filecontent, p_encoding);
        } catch (UnsupportedEncodingException e) {
            System.err.println("The OS does not support " + p_encoding);
            e.printStackTrace();
            return null;
        }
    }

    public static String readToString(File p_file, String p_encoding) {
        Long filelength = p_file.length();
        byte[] filecontent = new byte[filelength.intValue()];
        try {
            FileInputStream in = new FileInputStream(p_file);
            in.read(filecontent);
            in.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            return new String(filecontent, p_encoding);
        } catch (UnsupportedEncodingException e) {
            System.err.println("The OS does not support " + p_encoding);
            e.printStackTrace();
            return null;
        }
    }

    public static void mkdir(String p_dir) {
        File tmpFile = new File(p_dir);
        if (!tmpFile.exists() && !tmpFile.isDirectory()) {
            tmpFile.mkdirs();
        }
    }

    public static void writeFile(String p_filename, StringBuilder p_sb) throws Exception {
        File tmpFile = new File(p_filename);
        File parentDir = new File(tmpFile.getParent());
        if (!parentDir.exists()) {
            parentDir.mkdirs();
        }
        if (!tmpFile.exists()) {
            tmpFile.createNewFile();
        }
        PrintWriter tmpWriter = new PrintWriter(new FileWriter(tmpFile), true);
        tmpWriter.print(p_sb.toString());
        tmpWriter.close();
    }

    public static void writeFile(String p_filename, String p_text, String p_charset) throws Exception {
        File tmpFile = new File(p_filename);
        File parentDir = new File(tmpFile.getParent());
        if (!parentDir.exists()) {
            parentDir.mkdirs();
        }
        if (!tmpFile.exists()) {
            tmpFile.createNewFile();
        }
        PrintWriter tmpWriter = new PrintWriter(p_filename, p_charset);
        tmpWriter.print(p_text);
        tmpWriter.flush();
        tmpWriter.close();
    }

    public static void writeFile(File file, String p_text, String p_charset) throws Exception {
        File parentDir = new File(file.getParent());
        if (!parentDir.exists()) {
            parentDir.mkdirs();
        }
        if (!file.exists()) {
            file.createNewFile();
        }
        PrintWriter tmpWriter = new PrintWriter(file, p_charset);
        tmpWriter.print(p_text);
        tmpWriter.flush();
        tmpWriter.close();
    }

    public static void appendFile(String p_filename, String p_text, String p_charset) throws Exception {
        File tmpFile = new File(p_filename);
        File parentDir = new File(tmpFile.getParent());
        if (!parentDir.exists()) {
            parentDir.mkdirs();
        }
        if (!tmpFile.exists()) {
            tmpFile.createNewFile();
        }

        FileOutputStream fop = new FileOutputStream(tmpFile, true);
        fop.write(p_text.getBytes(p_charset));
        fop.flush();
        fop.close();
    }

    /**
     * 复制文件
     *
     * @throws IOException
     */
    public static void copyFile(File p_srcFile, File p_destFile) throws IOException {
        FileInputStream inStream = null;
        FileOutputStream outStream = null;
        FileChannel inChannel = null;
        FileChannel outChannel = null;
        try {
            inStream = new FileInputStream(p_srcFile);
            outStream = new FileOutputStream(p_destFile);
            inChannel = inStream.getChannel();
            outChannel = outStream.getChannel();
            inChannel.transferTo(0, inChannel.size(), outChannel);
        } catch (IOException e) {
            throw (e);
        } finally {
            try {
                if(inChannel != null){
                    inChannel.close();
                }if(inStream != null){
                    inStream.close();
                }if(outChannel != null){
                    outChannel.close();
                }if(outStream != null){
                    outStream.close();
                }
            } catch (IOException e) {
                throw (e);
            }
        }
    }

    public static byte[] toByteArray(String filename) throws IOException {

        File f = new File(filename);
        if (!f.exists()) {
            throw new FileNotFoundException(filename);
        }

        ByteArrayOutputStream bos = new ByteArrayOutputStream((int) f.length());
        BufferedInputStream in = null;
        try {
            in = new BufferedInputStream(new FileInputStream(f));
            int buf_size = 1024;
            byte[] buffer = new byte[buf_size];
            int len = 0;
            while (-1 != (len = in.read(buffer, 0, buf_size))) {
                bos.write(buffer, 0, len);
            }
            return bos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        } finally {
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            bos.close();
        }
    }

    public static String[] listFiles(String p_filepath) {
        if (p_filepath != null && !"".equals(p_filepath)) {
            File tmpFile = new File(p_filepath);
            if (tmpFile.exists() && tmpFile.isDirectory()) {
                return tmpFile.list();
            }
        }
        return null;
    }

    //获取文件夹目录下所有文件名
    public static List<String> listFilesByModDate(String dirPath) {
        List<String> fileNames = new ArrayList<String>();
        File file = new File(dirPath);
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            Arrays.sort(files, new Comparator<File>() {
                @Override
                public int compare(File f1, File f2) {
                    long diff = f1.lastModified() - f2.lastModified();
                    if (diff > 0) {
                        return -1;
                    } else if (diff == 0) {
                        return 0;
                    } else {
                        return 1;//如果 if 中修改为 返回-1 同时此处修改为返回 1  排序就会是递减
                    }
                }
                @Override
                public boolean equals(Object obj) {
                    return true;
                }
            });

            for (int i = 0; i < files.length; i++) {
                fileNames.add(files[i].getName());
            }
        }
        return fileNames;
    }

    /**
     * 复制文件
     *
     * @throws IOException
     */
    public static boolean moveFile(File p_srcFile, File p_destFile) throws Exception {
        try {
            if (!p_destFile.getParentFile().exists()) {
                p_destFile.getParentFile().mkdirs();
            }
            copyFile(p_srcFile, p_destFile);
            if (p_srcFile.delete()) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            if (p_destFile.exists()) {
                p_destFile.delete();
            }
            throw e;
        }

    }

    /**
     * 文件移动
     * @param source 原地址
     * @param target 新地址
     * @return
     */
    public static boolean renameTo(String source, String target) {
        File sourceFile=new File(target);
        if (!sourceFile.getParentFile().exists()) {
            sourceFile.getParentFile().mkdirs();
        }
        if(sourceFile.exists()){
            if(sourceFile.delete()){
            }else {
                return false;
            }
        }
        boolean flag = new File(source).renameTo(new File(target));
        return flag;
    }

    /**
     * 复制文件
     * @throws IOException
     */
    public static int copyFile(String p_srcFile, String p_destFile) throws IOException {
        try {
            File destFile = new File(p_destFile);
            if (destFile.exists()) {
                return 1;
            }else if(!destFile.getParentFile().exists()){
                destFile.getParentFile().mkdirs();
            }
            copyFile(new File(p_srcFile),destFile);
            return 0;
        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }
    }
}
