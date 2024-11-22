const forge = require('node-forge');

function generateCert() {
    const pki = forge.pki;
    const keypair = pki.rsa.generateKeyPair(2048);

    const csr = pki.createCertificationRequest();
    csr.publicKey = keypair.publicKey;
    csr.setSubject([{ name: 'commonName', value: 'Test Certificate' }]);
    csr.sign(keypair.privateKey);

    const cert = pki.createCertificate();
    cert.serialNumber = '01' + Math.floor(Math.random() * 1000000000);
    cert.validFrom = new Date();
    cert.validTo = new Date();
    cert.validTo.setFullYear(cert.validFrom.getFullYear() + 1);
    cert.setSubject([{ name: 'commonName', value: 'Test Certificate' }]);
    cert.setIssuer(csr.subject.attributes);
    cert.publicKey = csr.publicKey;
    cert.sign(keypair.privateKey);

    const certPem = pki.certificateToPem(cert);
    const privateKeyPem = pki.privateKeyToPem(keypair.privateKey);

    return certPem;
}

const result = generateCert();
console.log(result);
