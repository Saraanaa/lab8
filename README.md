# Lab 08 — Mockito mock объектын нэгж тест

## Оюутны мэдээлэл

- Нэр: Saraanaa
- Код: ____________________

## Орчин

```text
java -version
java version "22.0.2" 2024-07-16
Java(TM) SE Runtime Environment (build 22.0.2+9-70)
Java HotSpot(TM) 64-Bit Server VM (build 22.0.2+9-70, mixed mode, sharing)

Apache Maven 3.9.11
Java version: 22.0.2
OS name: Windows 11, arch: amd64
```

Тестийн бай нь `CourseRegistrationService`. `StudentRepository` болон `EmailSender` хоёр хамаарлыг бодит хэрэгжүүлэлтгүйгээр Mockito mock-оор орлуулсан тул тестүүд өгөгдлийн сан, SMTP серверээс бүрэн тусгаарлагдсан.

## Хэсэг A — 8 тестийн бүлэг

`CourseRegistrationServiceTest` дараах Mockito аргуудыг ашиглана.

| Тест | Шалгасан зүйл | Ашигласан хэрэгсэл |
|---|---|---|
| A1 | Идэвхтэй оюутны амжилттай бүртгэл | `when/thenReturn`, `verify` |
| A2 | Байхгүй оюутан | `verify(..., never())` |
| A3 | Идэвхгүй оюутан | `verifyNoInteractions` |
| A4 | Дутуу prerequisite | `when/thenReturn`, `never` |
| A5 | И-мэйлийн exception бүртгэлийг зогсоохгүй | `doThrow` |
| A6 | `studentId` болон `courseId`-ийн `null` оролт | `assertThrows` |
| A7 | И-мэйлийн body-д course ID орсон эсэх | `ArgumentCaptor` |
| A8 | Байхгүй оюутан, идэвхгүй оюутан, байхгүй хичээл | `@ParameterizedTest`, `@CsvSource` |

A8-ийн гурван мөр өөр өөр замаар явдаг. Ашиглагдахгүй мөрийн stub-ээс Mockito-ийн strict-stubbing алдаа гаргахгүйн тулд зөвхөн A8-д хамаарах stub-үүдийг `lenient()` болгосон.

Baseline үр дүн: **Tests run: 10, Failures: 0, Errors: 0, Skipped: 0 — BUILD SUCCESS**.

## Хэсэг B — mutation testing

Mutation бүрийн дараа `CourseRegistrationService.java`-г сэргээсэн. Үр дүнгийн бүрэн Maven гаралт `results/` хавтсанд хадгалагдсан.

| Mutation | Зориудын өөрчлөлт | Унасан тестүүд | Үр дүн |
|---|---|---|---|
| M1 | Идэвхгүй оюутны шалгалтыг устгасан | A3, A8-ийн “идэвхгүй оюутан” мөр | 10 тест, 2 failure, BUILD FAILURE |
| M2 | `saveRegistration()` дуудалтыг устгасан | A1, A5 | 10 тест, 2 failure, BUILD FAILURE |
| M3 | `courseId == null` шалгалтыг устгасан | A6 | 10 тест, 1 failure, BUILD FAILURE |

M3-ийн failure нь A6 хоёр өөр `null` оролтыг тусад нь шалгасантай холбоотой. Иймээс `studentId` болон `courseId` хоёулаа null үед `IllegalArgumentException` шидэгдэж, mutation илэрсэн.

## Дүгнэлт

Mockito mock нь хамаарлын бодит ажиллагааг ажиллуулахгүйгээр тухайн объект руу ирэх хариу болон үйлдлийг удирдах боломж олгодог. Гар аргаар бичсэн fake нь ихэвчлэн интерфейсийн бодитой жижиг хэрэгжүүлэлт шаарддаг бол mock нь тест бүрийн нөхцөлд динамикаар тохируулагдана. Энэ лабораторид repository-г database, email sender-ийг SMTP сервер гэж үзэн mock хийсэн. Бизнес логикийн өөрийнх нь дүрэм, жишээлбэл prerequisite шалгалтыг mock хийх шаардлагагүй. Харин database, network, clock зэрэг гаднын системийг unit test-д mock хийх нь зөв. `verify`-г хэтрүүлэн хэрэглэвэл implementation detail-д хэт баригдсан, жижиг refactor хийхэд эвдэрдэг тест үүснэ. Тиймээс зөвхөн хэрэглэгчийн үр дүн болон чухал side effect-үүдийг verify хийх хэрэгтэй. Mutation-ийн гурван туршилт бүгд failure үүсгэсэн нь тестүүд бизнес логикийн гол замуудыг хамгаалж байгааг харууллаа.
