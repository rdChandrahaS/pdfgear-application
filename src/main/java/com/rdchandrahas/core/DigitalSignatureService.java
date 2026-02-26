package com.rdchandrahas.core;

import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.PDSignature;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.SignatureInterface;
import org.bouncycastle.cert.jcajce.JcaCertStore;
import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.CMSSignedDataGenerator;
import org.bouncycastle.cms.CMSTypedData;
import org.bouncycastle.cms.jcajce.JcaSignerInfoGeneratorBuilder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DigitalSignatureService {

    private static final Logger LOGGER = Logger.getLogger(DigitalSignatureService.class.getName());

    public void signPdf(String inputPath, String outputPath, String keystorePath, String password) throws IOException,GeneralSecurityException {
        LOGGER.log(Level.INFO, "Starting Digital Signature for {0}", inputPath);

        KeyStore keystore = KeyStore.getInstance("PKCS12");
        char[] passwordChar = password.toCharArray();
        try (InputStream is = new FileInputStream(keystorePath)) {
            keystore.load(is, passwordChar);
        }

        String alias = keystore.aliases().nextElement();
        PrivateKey privateKey = (PrivateKey) keystore.getKey(alias, passwordChar);
        Certificate[] certificateChain = keystore.getCertificateChain(alias);

        try (PDDocument document = PDDocument.load(new File(inputPath), PdfService.getGlobalMemorySetting());
             FileOutputStream fos = new FileOutputStream(outputPath)) {

            PDSignature signature = new PDSignature();
            signature.setFilter(PDSignature.FILTER_ADOBE_PPKLITE);
            signature.setSubFilter(PDSignature.SUBFILTER_ADBE_PKCS7_DETACHED);
            signature.setName("Digital Signature Tool");
            signature.setLocation("Secured");
            signature.setReason("Document Authenticity verification");
            signature.setSignDate(Calendar.getInstance());

            SignatureInterface signatureInterface = content -> {
                try {
                    CMSSignedDataGenerator gen = new CMSSignedDataGenerator();
                    X509Certificate cert = (X509Certificate) certificateChain[0];
                    ContentSigner sha256Signer = new JcaContentSignerBuilder("SHA256WithRSA").build(privateKey);
                    
                    gen.addSignerInfoGenerator(new JcaSignerInfoGeneratorBuilder(
                            new JcaDigestCalculatorProviderBuilder().build()).build(sha256Signer, cert));
                    gen.addCertificates(new JcaCertStore(Arrays.asList(certificateChain)));
                    
                    CMSTypedData msg = new CMSProcessableByteArray(IOUtils.toByteArray(content));
                    CMSSignedData signedData = gen.generate(msg, false);
                    
                    return signedData.getEncoded();
                } catch (Exception e) {
                    throw new RuntimeException("Failed to generate CMS signature", e);
                }
            };

            document.addSignature(signature, signatureInterface);
            document.saveIncremental(fos);
        }
    }
}