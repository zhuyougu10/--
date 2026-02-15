const sharp = require('sharp');
const fs = require('fs');
const path = require('path');

const tabIcons = [
  { name: 'home', input: 'src/static/tab/home.svg', output: 'src/static/tab/home.png' },
  { name: 'home-active', input: 'src/static/tab/home-active.svg', output: 'src/static/tab/home-active.png' },
  { name: 'venue', input: 'src/static/tab/venue.svg', output: 'src/static/tab/venue.png' },
  { name: 'venue-active', input: 'src/static/tab/venue-active.svg', output: 'src/static/tab/venue-active.png' },
  { name: 'my', input: 'src/static/tab/my.svg', output: 'src/static/tab/my.png' },
  { name: 'my-active', input: 'src/static/tab/my-active.svg', output: 'src/static/tab/my-active.png' },
];

async function convertSvgToPng() {
  const baseDir = path.resolve(__dirname, '..');
  
  for (const icon of tabIcons) {
    const inputPath = path.join(baseDir, icon.input);
    const outputPath = path.join(baseDir, icon.output);
    
    try {
      await sharp(inputPath)
        .resize(81, 81)
        .png()
        .toFile(outputPath);
      console.log(`✓ Converted: ${icon.name}.svg -> ${icon.name}.png`);
    } catch (error) {
      console.error(`✗ Failed to convert ${icon.name}:`, error.message);
    }
  }
  
  console.log('\n转换完成！');
}

convertSvgToPng();
