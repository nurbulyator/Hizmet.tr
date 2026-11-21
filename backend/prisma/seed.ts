import { BookingStatus, Prisma, PrismaClient } from '@prisma/client';

const prisma = new PrismaClient();

// Demo client used in the presentation. Log in with this phone and code 1234.
const DEMO_PHONE = '+905321234567';

const categories = [
  { slug: 'manicure', nameEn: 'Manicure', nameTr: 'Manikür' },
  { slug: 'hair', nameEn: 'Hair', nameTr: 'Saç' },
  { slug: 'barber', nameEn: 'Barber', nameTr: 'Berber' },
  { slug: 'brows', nameEn: 'Brows & lashes', nameTr: 'Kaş ve kirpik' },
];

interface SeedSpecialist {
  name: string;
  title: string;
  category: string;
  experienceYears: number;
  city: string;
  district: string;
  address: string;
  rating: number;
  reviewCount: number;
  avatarColor: string;
  about: string;
  // [start hour, end hour] for Monday..Sunday, null = day off
  hours: ([number, number] | null)[];
  services: { name: string; durationMin: number; price: number }[];
}

const MON_SAT_10_19: SeedSpecialist['hours'] = [
  [10, 19],
  [10, 19],
  [10, 19],
  [10, 19],
  [10, 19],
  [10, 19],
  null,
];

const specialists: SeedSpecialist[] = [
  {
    name: 'Elif Yıldız',
    title: 'Manikür uzmanı',
    category: 'manicure',
    experienceYears: 6,
    city: 'İstanbul',
    district: 'Kadıköy',
    address: 'Moda Cad. 112, Kadıköy, İstanbul',
    rating: 4.9,
    reviewCount: 128,
    avatarColor: '#5C30D9',
    about:
      'Kalıcı oje, protez tırnak ve nail art. Steril aletler, kaliteli markalar.',
    hours: MON_SAT_10_19,
    services: [
      { name: 'Kalıcı oje ile manikür', durationMin: 90, price: 900 },
      { name: 'Kalıcı oje', durationMin: 60, price: 600 },
      { name: 'Protez tırnak', durationMin: 150, price: 1800 },
      { name: 'Kalıcı oje çıkarma', durationMin: 30, price: 250 },
    ],
  },
  {
    name: 'Zeynep Kaya',
    title: 'Kaş ve kirpik uzmanı',
    category: 'brows',
    experienceYears: 4,
    city: 'İstanbul',
    district: 'Beşiktaş',
    address: 'Ihlamurdere Cad. 45, Beşiktaş, İstanbul',
    rating: 4.8,
    reviewCount: 94,
    avatarColor: '#136850',
    about: 'Kaş alımı, boyama, laminasyon ve kirpik lifting.',
    hours: MON_SAT_10_19,
    services: [
      { name: 'Kaş alımı', durationMin: 30, price: 400 },
      { name: 'Kaş alımı ve boyama', durationMin: 60, price: 700 },
      { name: 'Kaş laminasyonu', durationMin: 60, price: 900 },
      { name: 'Kirpik lifting', durationMin: 90, price: 1100 },
    ],
  },
  {
    name: 'Mert Demir',
    title: 'Berber',
    category: 'barber',
    experienceYears: 9,
    city: 'İstanbul',
    district: 'Şişli',
    address: 'Halaskargazi Cad. 210, Şişli, İstanbul',
    rating: 4.9,
    reviewCount: 211,
    avatarColor: '#193ABD',
    about: 'Klasik ve modern erkek kesimleri, sakal tıraşı.',
    hours: [[9, 20], [9, 20], [9, 20], [9, 20], [9, 20], [10, 18], null],
    services: [
      { name: 'Saç kesimi', durationMin: 30, price: 500 },
      { name: 'Saç ve sakal', durationMin: 60, price: 800 },
      { name: 'Sakal tıraşı', durationMin: 30, price: 350 },
    ],
  },
  {
    name: 'Ayşe Korkmaz',
    title: 'Kuaför',
    category: 'hair',
    experienceYears: 11,
    city: 'İstanbul',
    district: 'Kadıköy',
    address: 'Bahariye Cad. 58, Kadıköy, İstanbul',
    rating: 4.7,
    reviewCount: 156,
    avatarColor: '#B4436C',
    about: 'Kesim, fön, boya ve keratin bakımı.',
    hours: MON_SAT_10_19,
    services: [
      { name: 'Kadın saç kesimi', durationMin: 60, price: 750 },
      { name: 'Fön', durationMin: 30, price: 350 },
      { name: 'Dip boya', durationMin: 120, price: 1500 },
      { name: 'Keratin bakımı', durationMin: 150, price: 2500 },
    ],
  },
  {
    name: 'Selin Arslan',
    title: 'Manikür ve pedikür uzmanı',
    category: 'manicure',
    experienceYears: 3,
    city: 'İstanbul',
    district: 'Beşiktaş',
    address: 'Çarşı Cad. 12, Beşiktaş, İstanbul',
    rating: 4.6,
    reviewCount: 47,
    avatarColor: '#C2410C',
    about: 'Manikür, pedikür ve el bakımı.',
    hours: [
      [11, 20],
      [11, 20],
      [11, 20],
      [11, 20],
      [11, 20],
      [11, 20],
      [12, 17],
    ],
    services: [
      { name: 'Klasik manikür', durationMin: 45, price: 450 },
      { name: 'Pedikür', durationMin: 60, price: 650 },
      { name: 'Kalıcı oje', durationMin: 60, price: 550 },
    ],
  },
  {
    name: 'Can Öztürk',
    title: 'Berber',
    category: 'barber',
    experienceYears: 5,
    city: 'İstanbul',
    district: 'Üsküdar',
    address: 'Hakimiyeti Milliye Cad. 33, Üsküdar, İstanbul',
    rating: 4.5,
    reviewCount: 63,
    avatarColor: '#0E7490',
    about: 'Fade kesim ve sakal şekillendirme.',
    hours: MON_SAT_10_19,
    services: [
      { name: 'Saç kesimi', durationMin: 30, price: 400 },
      { name: 'Fade kesim', durationMin: 45, price: 550 },
      { name: 'Saç ve sakal', durationMin: 60, price: 700 },
    ],
  },
  {
    name: 'Burak Şahin',
    title: 'Berber',
    category: 'barber',
    experienceYears: 7,
    city: 'Ankara',
    district: 'Çankaya',
    address: 'Tunalı Hilmi Cad. 88, Çankaya, Ankara',
    rating: 4.8,
    reviewCount: 102,
    avatarColor: '#4D7C0F',
    about: 'Erkek kuaförü, klasik tıraş.',
    hours: MON_SAT_10_19,
    services: [
      { name: 'Saç kesimi', durationMin: 30, price: 450 },
      { name: 'Saç ve sakal', durationMin: 60, price: 750 },
    ],
  },
  {
    name: 'Deniz Aydın',
    title: 'Kuaför',
    category: 'hair',
    experienceYears: 8,
    city: 'İzmir',
    district: 'Alsancak',
    address: 'Kıbrıs Şehitleri Cad. 140, Alsancak, İzmir',
    rating: 4.7,
    reviewCount: 88,
    avatarColor: '#7C3AED',
    about: 'Saç kesimi, boya ve bakım.',
    hours: MON_SAT_10_19,
    services: [
      { name: 'Kadın saç kesimi', durationMin: 60, price: 650 },
      { name: 'Boya', durationMin: 120, price: 1300 },
    ],
  },
];

const otherClients = [
  { phone: '+905301110001', name: 'Merve Ö.' },
  { phone: '+905301110002', name: 'Ece B.' },
  { phone: '+905301110003', name: 'Selin T.' },
];

const reviewTexts = [
  { rating: 5, comment: 'Çok memnun kaldım, kesinlikle tekrar geleceğim!' },
  { rating: 5, comment: 'Temiz, özenli ve tam zamanında.' },
  { rating: 4, comment: 'Güzel oldu, sadece biraz bekledim.' },
];

/** Date in Istanbul (UTC+3): today + days, at the given hour and minute. */
function istanbul(days: number, hour: number, minute = 0): Date {
  const nowInIstanbul = new Date(Date.now() + 3 * 3_600_000);
  const date = new Date(
    Date.UTC(
      nowInIstanbul.getUTCFullYear(),
      nowInIstanbul.getUTCMonth(),
      nowInIstanbul.getUTCDate() + days,
      hour - 3,
      minute,
    ),
  );
  return date;
}

/** Next working day (Mon-Sat) starting from today + days. */
function workingDay(days: number): number {
  let d = days;
  while (istanbul(d, 12).getUTCDay() === 0) d++;
  return d;
}

async function main() {
  await prisma.review.deleteMany();
  await prisma.booking.deleteMany();
  await prisma.favorite.deleteMany();
  await prisma.refreshToken.deleteMany();
  await prisma.workingHours.deleteMany();
  await prisma.service.deleteMany();
  await prisma.specialist.deleteMany();
  await prisma.category.deleteMany();
  await prisma.user.deleteMany();

  const categoryIds = new Map<string, number>();
  for (const c of categories) {
    const created = await prisma.category.create({ data: c });
    categoryIds.set(c.slug, created.id);
  }

  type SpecialistWithServices = Prisma.SpecialistGetPayload<{
    include: { services: true };
  }>;
  const created: SpecialistWithServices[] = [];
  for (const s of specialists) {
    const { hours, services, category, ...data } = s;
    const specialist = await prisma.specialist.create({
      data: {
        ...data,
        categoryId: categoryIds.get(category)!,
        services: { create: services },
        workingHours: {
          create: hours.flatMap((h, i) =>
            h
              ? [
                  {
                    weekday: i + 1,
                    startMinute: h[0] * 60,
                    endMinute: h[1] * 60,
                  },
                ]
              : [],
          ),
        },
      },
      include: { services: true },
    });
    created.push(specialist);
  }

  const demo = await prisma.user.create({
    data: { phone: DEMO_PHONE, name: 'Ayşe' },
  });
  const clients: { id: number }[] = [];
  for (const c of otherClients) {
    clients.push(await prisma.user.create({ data: c }));
  }

  // Past visits with reviews, so the "Reviews" tab is not empty.
  for (const specialist of created) {
    for (let i = 0; i < reviewTexts.length; i++) {
      const service = specialist.services[i % specialist.services.length];
      const startsAt = istanbul(-(i * 4 + 3), 11);
      const booking = await prisma.booking.create({
        data: {
          clientId: clients[i].id,
          specialistId: specialist.id,
          serviceId: service.id,
          startsAt,
          endsAt: new Date(startsAt.getTime() + service.durationMin * 60_000),
          price: service.price,
          status: BookingStatus.COMPLETED,
        },
      });
      await prisma.review.create({
        data: {
          bookingId: booking.id,
          clientId: clients[i].id,
          specialistId: specialist.id,
          ...reviewTexts[i],
          createdAt: startsAt,
        },
      });
    }
  }

  const [elif, zeynep] = created;
  const book = (
    clientId: number,
    specialist: SpecialistWithServices,
    serviceIndex: number,
    startsAt: Date,
    status: BookingStatus,
  ) => {
    const service = specialist.services[serviceIndex];
    return prisma.booking.create({
      data: {
        clientId,
        specialistId: specialist.id,
        serviceId: service.id,
        startsAt,
        endsAt: new Date(startsAt.getTime() + service.durationMin * 60_000),
        price: service.price,
        status,
      },
    });
  };

  // Demo client: "My appointments" screen.
  const d1 = workingDay(1);
  const d2 = workingDay(d1 + 1);
  await book(demo.id, elif, 0, istanbul(d1, 10), BookingStatus.PENDING);
  await book(demo.id, zeynep, 1, istanbul(d2, 18), BookingStatus.CONFIRMED);
  await book(demo.id, elif, 1, istanbul(-2, 14), BookingStatus.COMPLETED);
  await prisma.favorite.create({
    data: { userId: demo.id, specialistId: zeynep.id },
  });

  // Other clients: some of Elif's slots are already taken.
  for (const day of [d1, d2]) {
    await book(
      clients[0].id,
      elif,
      1,
      istanbul(day, 12),
      BookingStatus.CONFIRMED,
    );
    await book(
      clients[1].id,
      elif,
      0,
      istanbul(day, 15),
      BookingStatus.PENDING,
    );
    await book(
      clients[2].id,
      elif,
      3,
      istanbul(day, 17, 30),
      BookingStatus.CONFIRMED,
    );
  }

  console.log(`Seed done. Demo login: ${DEMO_PHONE}, code 1234`);
}

main()
  .catch((e) => {
    console.error(e);
    process.exit(1);
  })
  .finally(() => prisma.$disconnect());
