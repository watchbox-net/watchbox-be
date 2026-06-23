package net.watchbox.domain.notification.webpush.util;

import nl.martijndwars.webpush.Utils;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.interfaces.ECPrivateKey;
import org.bouncycastle.jce.interfaces.ECPublicKey;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECParameterSpec;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.Security;
import java.util.Base64;

/**
 * VAPID 키쌍을 1회 발급하기 위한 유틸. IDE 에서 main 실행.
 *
 * <p>출력된 두 값을 환경변수로 설정:
 * <pre>
 *   WEB_PUSH_PUBLIC_KEY=...
 *   WEB_PUSH_PRIVATE_KEY=...
 * </pre>
 *
 * <p>publicKey 는 프론트엔드의 PushManager.subscribe(applicationServerKey: ...) 에도 사용됨.
 */
public class WebPushVapidKeyGenerator {

    public static void main(String[] args) throws Exception {
        Security.addProvider(new BouncyCastleProvider());

        ECParameterSpec parameterSpec = ECNamedCurveTable.getParameterSpec(Utils.CURVE);
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(Utils.ALGORITHM, BouncyCastleProvider.PROVIDER_NAME);
        keyPairGenerator.initialize(parameterSpec);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();

        byte[] publicKeyBytes = Utils.encode((ECPublicKey) keyPair.getPublic());
        byte[] privateKeyBytes = Utils.encode((ECPrivateKey) keyPair.getPrivate());

        String publicKey = Base64.getUrlEncoder().withoutPadding().encodeToString(publicKeyBytes);
        String privateKey = Base64.getUrlEncoder().withoutPadding().encodeToString(privateKeyBytes);

        System.out.println("=== VAPID Keys ===");
        System.out.println("WEB_PUSH_PUBLIC_KEY=" + publicKey);
        System.out.println("WEB_PUSH_PRIVATE_KEY=" + privateKey);
        System.out.println();
        System.out.println("Frontend applicationServerKey (publicKey) 값으로 사용:");
        System.out.println(publicKey);
    }
}
