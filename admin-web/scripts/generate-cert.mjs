import fs from 'fs'
import path from 'path'
import selfsigned from 'selfsigned'

const certDir = path.resolve(process.cwd(), '.cert')
if (!fs.existsSync(certDir)) {
  fs.mkdirSync(certDir, { recursive: true })
}

const attrs = [{ name: 'commonName', value: '192.168.10.9' }]
const extensions = [
  {
    name: 'subjectAltName',
    altNames: [
      { type: 2, value: 'localhost' },
      { type: 7, ip: '127.0.0.1' },
      { type: 7, ip: '192.168.10.9' }
    ]
  }
]

const pems = await selfsigned.generate(attrs, {
  keySize: 2048,
  days: 3650,
  algorithm: 'sha256',
  extensions
})

fs.writeFileSync(path.resolve(certDir, 'dev-key.pem'), pems.private || pems.privateKey)
fs.writeFileSync(path.resolve(certDir, 'dev-cert.pem'), pems.cert)

console.log('Generated certificate files:')
console.log(path.resolve(certDir, 'dev-key.pem'))
console.log(path.resolve(certDir, 'dev-cert.pem'))
