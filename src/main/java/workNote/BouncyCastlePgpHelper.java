package workNote;

import org.bouncycastle.bcpg.ArmoredOutputStream;
import org.bouncycastle.bcpg.BCPGOutputStream;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openpgp.*;
import org.bouncycastle.openpgp.jcajce.JcaPGPObjectFactory;
import org.bouncycastle.openpgp.operator.bc.BcPGPDataEncryptorBuilder;
import org.bouncycastle.openpgp.operator.bc.BcPublicKeyKeyEncryptionMethodGenerator;
import org.bouncycastle.openpgp.operator.jcajce.*;
import org.bouncycastle.util.io.Streams;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.charset.Charset;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.Security;
import java.util.Date;
import java.util.Iterator;

/**
 * Created by F on 2019/09/04.
 */
public class BouncyCastlePgpHelper {
    static {
        //Security.addProvider(new BouncyCastleProvider());
        //增加加密提供者
        Security.insertProviderAt(new BouncyCastleProvider(),1);
    }

    /**
     * 解除秘钥长度限制:
     * 1、通过反射移除了isRestricted 的变量修饰符：final
     * 2、然后将isRestricted 赋值为false
     */
    static {
        try {
            Class<?> clazz = Class.forName("javax.crypto.JceSecurity");
            Field nameField = clazz.getDeclaredField("isRestricted");

            Field modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
            modifiersField.setInt(nameField, nameField.getModifiers() & ~Modifier.FINAL);

            nameField.setAccessible(true);
            nameField.set(null, Boolean.FALSE);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static PGPPublicKey readPublicKey(InputStream input) throws IOException, PGPException {

        PGPPublicKeyRingCollection pgpPub = new PGPPublicKeyRingCollection(PGPUtil.getDecoderStream(input), new JcaKeyFingerprintCalculator());
        PGPPublicKey key = pgpPub.getKeyRings().next().getPublicKeys().next();

        if (key.isEncryptionKey())
            return key;

        throw new IllegalArgumentException("Can't find encryption key in key ring.");
    }


    public static PGPSecretKey readSecretKey(InputStream in) throws Exception {

        PGPSecretKeyRingCollection pgpSec = new PGPSecretKeyRingCollection(PGPUtil.getDecoderStream(in), new JcaKeyFingerprintCalculator());
        PGPSecretKey key = pgpSec.getKeyRings().next().getSecretKeys().next();

        if (key.isSigningKey())
            return key;

        throw new IllegalArgumentException("Can't find signing key in key ring.");
    }

    public static PGPPrivateKey findPrivateKey(InputStream in, char[] pass) throws Exception {

        PGPSecretKey pgpSecKey = readSecretKey(in);

        return pgpSecKey.extractPrivateKey(new JcePBESecretKeyDecryptorBuilder().setProvider("BC").build(pass));
    }

    public static byte[] compressSigned(byte[] clearData, String fileName, PGPPublicKey publicKey, PGPSecretKey secretKey,
                                        PGPPrivateKey privateKey) throws Exception {

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        PGPSignatureGenerator sGen =
                new PGPSignatureGenerator(new JcaPGPContentSignerBuilder(secretKey.getPublicKey().getAlgorithm(), PGPUtil.SHA256).setProvider("BC"));

        sGen.init(PGPSignature.BINARY_DOCUMENT, privateKey);

        Iterator<String> it = secretKey.getPublicKey().getUserIDs();
        if (it.hasNext()) {
            PGPSignatureSubpacketGenerator spGen = new PGPSignatureSubpacketGenerator();

            spGen.setSignerUserID(false, it.next());
            sGen.setHashedSubpackets(spGen.generate());
        }

        PGPCompressedDataGenerator cGen = new PGPCompressedDataGenerator(PGPCompressedData.ZIP);
        BCPGOutputStream bOut = new BCPGOutputStream(cGen.open(out));
        sGen.generateOnePassVersion(true).encode(bOut);

        PGPLiteralDataGenerator lData = new PGPLiteralDataGenerator();
        OutputStream lOut = lData.open(bOut, PGPLiteralData.BINARY, "pgp", clearData.length, new Date());
        lOut.write(clearData);
        lData.close();

        sGen.update(clearData);
        sGen.generate().encode(bOut);

        cGen.close();

        return out.toByteArray();
    }

    public static String encryptSigned(String payload, PGPPublicKey publicKey, PGPSecretKey secretKey, PGPPrivateKey privateKey)
            throws Exception {

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ArmoredOutputStream armored = new ArmoredOutputStream(out);

        //byte[] compressed = compressSigned(payload.getBytes(), "pgp", publicKey, secretKey, privateKey);
        System.err.println("系统默认字符编码：" + Charset.defaultCharset());
        byte[] compressed = compressSigned(payload.getBytes("UTF-8"), "pgp", publicKey, secretKey, privateKey);

        PGPEncryptedDataGenerator cPk =
                new PGPEncryptedDataGenerator(new BcPGPDataEncryptorBuilder(PGPEncryptedData.AES_256).setSecureRandom(new SecureRandom())
                        .setWithIntegrityPacket(true));
        cPk.addMethod(new BcPublicKeyKeyEncryptionMethodGenerator(publicKey));

        OutputStream cOut = cPk.open(armored, compressed.length);
        cOut.write(compressed);

        cOut.close();
        armored.close();

        return out.toString("utf-8");
    }

    public static String decryptAndVerify(String encrypted, PGPPrivateKey privateKey, PGPPublicKey key) throws Exception {

        JcaPGPObjectFactory objectFactory =
                new JcaPGPObjectFactory(PGPUtil.getDecoderStream(new ByteArrayInputStream(encrypted.getBytes("utf-8"))));

        Object obj = objectFactory.nextObject();
        PGPEncryptedDataList enc =
                obj instanceof PGPEncryptedDataList ? (PGPEncryptedDataList) obj : (PGPEncryptedDataList) objectFactory.nextObject();
        PGPPublicKeyEncryptedData pbe = (PGPPublicKeyEncryptedData) enc.getEncryptedDataObjects().next();

        InputStream clear = pbe.getDataStream(new JcePublicKeyDataDecryptorFactoryBuilder().setProvider("BC").build(privateKey));
        objectFactory = new JcaPGPObjectFactory(clear);
        Object message = objectFactory.nextObject();

        objectFactory = new JcaPGPObjectFactory(((PGPCompressedData) message).getDataStream());

        PGPOnePassSignature ops = ((PGPOnePassSignatureList) objectFactory.nextObject()).get(0);
        ops.init(new JcaPGPContentVerifierBuilderProvider().setProvider("BC"), key);

        InputStream input = ((PGPLiteralData) objectFactory.nextObject()).getInputStream();
        byte[] decrypted = Streams.readAll(input);
        ops.update(decrypted);

        if (!ops.verify(((PGPSignatureList) objectFactory.nextObject()).get(0)))
            throw new Exception("pgp verify sign error.");

        return new String(decrypted, "utf-8");
    }



    public static String decrypt(String encrypted, PGPPrivateKey privateKey) throws Exception {

        JcaPGPObjectFactory objectFactory =
                new JcaPGPObjectFactory(PGPUtil.getDecoderStream(new ByteArrayInputStream(encrypted.getBytes("utf-8"))));

        Object obj = objectFactory.nextObject();
        PGPEncryptedDataList enc =
                obj instanceof PGPEncryptedDataList ? (PGPEncryptedDataList) obj : (PGPEncryptedDataList) objectFactory.nextObject();
        PGPPublicKeyEncryptedData pbe = (PGPPublicKeyEncryptedData) enc.getEncryptedDataObjects().next();

        InputStream clear = pbe.getDataStream(new JcePublicKeyDataDecryptorFactoryBuilder().setProvider("BC").build(privateKey));
        objectFactory = new JcaPGPObjectFactory(clear);
        Object message = objectFactory.nextObject();

        objectFactory = new JcaPGPObjectFactory(((PGPCompressedData) message).getDataStream());

//	    PGPOnePassSignature ops = ((PGPOnePassSignatureList) objectFactory.nextObject()).get(0);
//	    ops.init(new JcaPGPContentVerifierBuilderProvider().setProvider("BC"), key);

        InputStream input = ((PGPLiteralData) objectFactory.nextObject()).getInputStream();
        byte[] decrypted = Streams.readAll(input);
//	    ops.update(decrypted);

//	    if (!ops.verify(((PGPSignatureList) objectFactory.nextObject()).get(0)))
//	      throw new Exception("pgp verify sign error.");

        return new String(decrypted, "utf-8");
    }

    private static PGPPrivateKey findSecretKey(PGPSecretKeyRingCollection  pgpSec, long keyID, char[] pass)
            throws PGPException, NoSuchProviderException {
        PGPSecretKey pgpSecKey = pgpSec.getSecretKey(keyID);
        if (pgpSecKey == null){
            return null;
        } else {
            return pgpSecKey.extractPrivateKey(new JcePBESecretKeyDecryptorBuilder().setProvider("BC").build(pass));
        }
    }

    private static PGPPublicKey readPublicKeyFromCol(InputStream in)throws Exception {
        PGPPublicKeyRing pkRing = null;
        PGPPublicKeyRingCollection pkCol = new PGPPublicKeyRingCollection(PGPUtil.getDecoderStream(in), new JcaKeyFingerprintCalculator());
        Iterator it = pkCol.getKeyRings();
        while (it.hasNext()) {
            pkRing = (PGPPublicKeyRing) it.next();
            Iterator pkIt = pkRing.getPublicKeys();
            while (pkIt.hasNext()) {
                PGPPublicKey key = (PGPPublicKey) pkIt.next();
                if (key.isMasterKey())
                    return key;
            }
        }
        return null;
    }

    /**
     * gpg解密验签
     * @param bankPubFilePath
     * @param orgPrvFilePath
     * @param passwd
     * @param decryptFilePath
     * @param resultFilePath
     * @throws Exception
     */
    public static void decryptFile(String bankPubFilePath,String orgPrvFilePath,char[] passwd,String decryptFilePath,String resultFilePath)throws Exception{
        //私钥
        InputStream keyIn = new BufferedInputStream (new FileInputStream(orgPrvFilePath));
        //待解密文件
        InputStream in = new BufferedInputStream (new FileInputStream(decryptFilePath));
        try{
            in = PGPUtil.getDecoderStream(in);
            PGPObjectFactory pgpF = new PGPObjectFactory(in, new JcaKeyFingerprintCalculator());
            Object o = pgpF.nextObject();
            PGPEncryptedDataList enc = o instanceof PGPEncryptedDataList?(PGPEncryptedDataList)o : (PGPEncryptedDataList)pgpF.nextObject();
            Iterator it = enc.getEncryptedDataObjects();
            PGPPrivateKey sKey = null;
            PGPPublicKeyEncryptedData pbe = null;
            PGPSecretKeyRingCollection  pgpSec = new PGPSecretKeyRingCollection(PGPUtil.getDecoderStream(keyIn), new JcaKeyFingerprintCalculator());

            while (sKey == null && it.hasNext()){
                pbe = (PGPPublicKeyEncryptedData)it.next();
                sKey = findSecretKey(pgpSec, pbe.getKeyID(), passwd);
            }
            if (sKey == null){
                throw new IllegalArgumentException("secret key for message not found.");
            }
            InputStream clear = pbe.getDataStream(new JcePublicKeyDataDecryptorFactoryBuilder().setProvider("BC").build(sKey));
            PGPObjectFactory plainFact = new PGPObjectFactory(clear, new JcaKeyFingerprintCalculator());
            Object message = plainFact.nextObject();
            if (message instanceof PGPCompressedData){
                PGPCompressedData   cData = (PGPCompressedData)message;
                PGPObjectFactory  pgpFact = new PGPObjectFactory(cData.getDataStream(), new JcaKeyFingerprintCalculator());
                message = pgpFact.nextObject();
                if (message instanceof PGPLiteralData){
                    PGPLiteralData      ld = (PGPLiteralData)message;
                    FileOutputStream    fOut = new FileOutputStream(resultFilePath);
                    InputStream    unc = ld.getInputStream();
                    int    ch;
                    while ((ch = unc.read()) >= 0){
                        fOut.write(ch);
                    }
                }else if (message instanceof PGPOnePassSignatureList) {
                    PGPOnePassSignatureList p1 = (PGPOnePassSignatureList) message;
                    InputStream pubKey = new BufferedInputStream (new FileInputStream(bankPubFilePath));
                    PGPPublicKey key =readPublicKeyFromCol(pubKey);
                    if(key!=null){
                        PGPOnePassSignature ops = p1.get(0);
                        ops.init(new JcaPGPContentVerifierBuilderProvider().setProvider("BC"), key);
                        // file output dirctory
                        FileOutputStream out = new FileOutputStream(resultFilePath);
                        PGPLiteralData  p2 = (PGPLiteralData) pgpFact.nextObject();
                        int ch;
                        InputStream  dIn = p2.getInputStream();
                        while ((ch = dIn.read()) >= 0) {
                            ops.update((byte)ch);
                            out.write(ch);
                        }
                        out.close();
                    } else {
                        throw new PGPException ("unable to find public key for signed file");
                    }
                } else {
                    throw new PGPException("message is not a simple encrypted file - type unknown.");
                }
            } else {
                throw new PGPException ("unable to verify message");
            }
        }catch (PGPException e){
            if (e.getUnderlyingException() != null){
                e.getUnderlyingException().printStackTrace();
            }
        }finally {
            in.close();
            keyIn.close();
        }
    }
}
