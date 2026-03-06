# 🔇 SoundBlocker v2.0 — Paper 1.21.1

بلوجن لحجب وتبديل أصوات Minecraft على سيرفرات Paper.

---

## ✅ الفيتشرز
- **حجب** أصوات كاملة (غنم، ضفدع، خفاش، كهوف...)
- **تبديل** صوت بصوت تاني مع التحكم في الفوليوم والبيتش
- كل حاجة تتعدل من **config.yml** أو **كوماندات** مباشرة
- التغييرات بتتحفظ أوتوماتيك في الكونفيج

---

## 📋 الكوماندات

| الكوماند | الوصف |
|----------|-------|
| `/sb block <صوت>` | حجب صوت |
| `/sb unblock <صوت>` | فك حجب صوت |
| `/sb replace <من> <إلى> [vol] [pitch]` | تبديل صوت بصوت تاني |
| `/sb unreplace <صوت>` | فك تبديل صوت |
| `/sb list` | عرض كل الأصوات المحجوبة والمبدلة |
| `/sb reload` | إعادة تحميل الكونفيج |

**البيرمشن:** `soundblocker.admin` (افتراضي: OP)

---

## ⚙️ config.yml

```yaml
# حجب أصوات
blocked-sounds:
  - entity.bat.ambient
  - ambient.cave.cave1

# تبديل أصوات
replaced-sounds:
  "entity.sheep.ambient":
    sound: "entity.cow.ambient"
    volume: 1.0
    pitch: 1.0
```

---

## 🔨 البيلد على GitHub

كل ما تعمل `push` على `main`:
1. روح **Actions** في الريبو
2. افتح آخر workflow
3. حمل الـ JAR من **Artifacts**

### بيلد يدوي:
```bash
mvn clean package
# الـ JAR في: target/SoundBlocker-2.0.0.jar
```

---

## 📦 التثبيت
1. حط الـ JAR في `plugins/`
2. شغل السيرفر
3. عدل `plugins/SoundBlocker/config.yml`
4. `/sb reload`
