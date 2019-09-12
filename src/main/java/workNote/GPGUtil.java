package workNote;

import org.bouncycastle.openpgp.*;

import java.io.*;

public class GPGUtil {

    /**
     * GPG加密
     * @param encryFilePath
     * @param resultFilePath
     * @param charset
     * @return
     * @throws Exception
     */
    public static boolean pgpEncryptSigned(String bankPubFilePath,String orgPrvFilePath
            ,char[] passwd,String encryFilePath,String resultFilePath,String charset){
        try {
            //待加密内容
            String data=FileHelper.readFileText(encryFilePath,charset);
            //银行公钥
            PGPPublicKey dbsPubKey = BouncyCastlePgpHelper.readPublicKey(new FileInputStream(bankPubFilePath));
            //客户私钥
            PGPSecretKey glpSecretKey = BouncyCastlePgpHelper.readSecretKey(new FileInputStream(orgPrvFilePath));
            PGPPrivateKey glpPrivateKey = BouncyCastlePgpHelper.findPrivateKey(new FileInputStream(orgPrvFilePath), passwd);
            //加密
            String encryptContent= BouncyCastlePgpHelper.encryptSigned(data, dbsPubKey, glpSecretKey, glpPrivateKey);
            //写入文件
            FileHelper.writeFile(resultFilePath,encryptContent,charset);
            //判断解密结果
            if(StringHelper.hasAnyChar(FileHelper.readFileText(resultFilePath,charset))){
                return true;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }

    /**
     * GPG解密
     * @param bankPubFilePath
     * @param orgPrvFilePath
     * @param passwd
     * @param decryptFilePath
     * @param resultFilePath
     * @param charset
     * @return
     */
    public static boolean pgpDecryptAndVerify(String bankPubFilePath,String orgPrvFilePath
            ,char[] passwd,String decryptFilePath,String resultFilePath,String charset){
        try {
            BouncyCastlePgpHelper.decryptFile(bankPubFilePath,orgPrvFilePath,passwd,decryptFilePath,resultFilePath);
            //判断解密结果
            if (StringHelper.hasAnyChar(FileHelper.readFileText(resultFilePath, charset))) {
                return true;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }

    public static void main(String[] args) {
        String bankPubFilePath="";//银行公钥
        String orgPrvFilePath="";//企业私钥
        String passwd="123456";//企业私钥密码
        String encryFilePath="";//待处理文件
        String resultFilePath="";//结果文件
        //加密
        pgpEncryptSigned(bankPubFilePath,orgPrvFilePath,passwd.toCharArray(),encryFilePath,resultFilePath,"utf-8");
        //解密
        pgpDecryptAndVerify(bankPubFilePath,orgPrvFilePath,passwd.toCharArray(),encryFilePath,resultFilePath,"utf-8");
    }
}
