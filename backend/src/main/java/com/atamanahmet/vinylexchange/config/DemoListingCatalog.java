package com.atamanahmet.vinylexchange.config;

import java.util.List;
import java.util.UUID;

import com.atamanahmet.vinylexchange.domain.embeddable.MediaInfo;
import com.atamanahmet.vinylexchange.domain.enums.Country;
import com.atamanahmet.vinylexchange.domain.enums.MediaFormat;
import com.atamanahmet.vinylexchange.domain.enums.VinylSubtype;

/**
 * Fixed demo listing ids and metadata for local dev seeding.
 * Ids match demo/covers/{uuid}/ folders in resources and uploads/listings/{uuid}/.
 */
public final class DemoListingCatalog {

    private DemoListingCatalog() {
    }

    public record DemoListing(
            UUID id,
            String title,
            String artistName,
            String labelName,
            MediaInfo mediaInfo,
            Country country,
            int year,
            String condition,
            long priceKurus,
            List<String> genreNames) {
    }

    public static MediaInfo vinylLp(int discCount) {
        return MediaInfo.builder()
                .format(MediaFormat.VINYL)
                .vinylSubtype(VinylSubtype.LP)
                .speedRpm(33)
                .vinylSize("12\"")
                .discCount(discCount)
                .build();
    }

    public static final UUID ABBEY_ROAD = UUID.fromString("a1000001-0001-4001-8001-000000000001");
    public static final UUID NEVERMIND = UUID.fromString("a1000002-0002-4002-8002-000000000002");
    public static final UUID OK_COMPUTER = UUID.fromString("a1000003-0003-4003-8003-000000000003");
    public static final UUID IN_RAINBOWS = UUID.fromString("a1000004-0004-4004-8004-000000000004");
    public static final UUID THE_WALL = UUID.fromString("a1000005-0005-4005-8005-000000000005");
    public static final UUID KIND_OF_BLUE = UUID.fromString("a1000006-0006-4006-8006-000000000006");
    public static final UUID BLUE_TRAIN = UUID.fromString("a1000007-0007-4007-8007-000000000007");
    public static final UUID BACK_TO_BLACK = UUID.fromString("a1000008-0008-4008-8008-000000000008");
    public static final UUID RUMOURS = UUID.fromString("a1000009-0009-4009-8009-000000000009");
    public static final UUID LED_ZEPPELIN_IV = UUID.fromString("a1000010-0010-4010-8010-000000000010");
    public static final UUID MASTER_OF_PUPPETS = UUID.fromString("a1000011-0011-4011-8011-000000000011");
    public static final UUID REVOLVER = UUID.fromString("a1000012-0012-4012-8012-000000000012");
    public static final UUID UNKNOWN_PLEASURES = UUID.fromString("a1000013-0013-4013-8013-000000000013");
    public static final UUID DISINTEGRATION = UUID.fromString("a1000014-0014-4014-8014-000000000014");
    public static final UUID ZIGGY_STARDUST = UUID.fromString("a1000015-0015-4015-8015-000000000015");
    public static final UUID DISCOVERY = UUID.fromString("a1000016-0016-4016-8016-000000000016");
    public static final UUID TO_PIMP_A_BUTTERFLY = UUID.fromString("a1000017-0017-4017-8017-000000000017");
    public static final UUID MEZZANINE = UUID.fromString("a1000018-0018-4018-8018-000000000018");
    public static final UUID VIOLATOR = UUID.fromString("a1000019-0019-4019-8019-000000000019");

    public static final UUID HIGHWAY_STAR_BELTER = UUID.fromString("a2000001-0001-4001-8001-000000000020");
    public static final UUID HIGHWAY_STAR = UUID.fromString("a2000002-0002-4002-8002-000000000002");
    public static final UUID WHEN_A_BLIND_MAN_CRIES = UUID.fromString("a2000003-0003-4003-8003-000000000003");
    public static final UUID CHILD_IN_TIME = UUID.fromString("a2000004-0004-4004-8004-000000000004");
    public static final UUID PERFECT_STRANGERS = UUID.fromString("a2000005-0005-4005-8005-000000000005");
    public static final UUID WONDERFUL_TONIGHT = UUID.fromString("a2000006-0006-4006-8006-000000000006");
    public static final UUID TAKE_ME_AWAY = UUID.fromString("a2000007-0007-4007-8007-000000000007");
    public static final UUID LAST_DAYS_OF_MAY = UUID.fromString("a2000008-0008-4008-8008-000000000008");
    public static final UUID DONT_FEAR_THE_REAPER = UUID.fromString("a2000009-0009-4009-8009-000000000009");
    public static final UUID SPACE_ODDITY = UUID.fromString("a2000010-0010-4010-8010-000000000010");
    public static final UUID ALL_THE_MADMEN = UUID.fromString("a2000011-0011-4011-8011-000000000011");
    public static final UUID WAR_PIGS = UUID.fromString("a2000012-0012-4012-8012-000000000012");
    public static final UUID ACHILLES_LAST_STAND = UUID.fromString("a2000013-0013-4013-8013-000000000013");
    public static final UUID CARRY_ON_WAYWARD_SON = UUID.fromString("a2000014-0014-4014-8014-000000000014");
    public static final UUID CRAZY_ON_YOU = UUID.fromString("a2000015-0015-4015-8015-000000000015");
    public static final UUID LADY_FANTASY = UUID.fromString("a2000016-0016-4016-8016-000000000016");
    public static final UUID GATES_OF_BABYLON = UUID.fromString("a2000017-0017-4017-8017-000000000017");
    public static final UUID HAPPY_TOGETHER = UUID.fromString("a2000018-0018-4018-8018-000000000018");
    public static final UUID WHILE_MY_GUITAR_RRHOF = UUID.fromString("a2000019-0019-4019-8019-000000000019");
    public static final UUID WATCHTOWER_BSG = UUID.fromString("a2000020-0020-4020-8020-000000000020");
    public static final UUID LOVE_IS_LIKE_OXYGEN = UUID.fromString("a2000021-0021-4021-8021-000000000021");
    public static final UUID SHINE_ON_YOU_CRAZY_DIAMOND = UUID.fromString("a2000022-0022-4022-8022-000000000022");
    public static final UUID MAN_WHO_SOLD_THE_WORLD = UUID.fromString("a2000023-0023-4023-8023-000000000023");
    public static final UUID THE_CHAIN = UUID.fromString("a2000024-0024-4024-8024-000000000024");
    public static final UUID HAVE_A_CIGAR = UUID.fromString("a2000025-0025-4025-8025-000000000025");
    public static final UUID WILD_WORLD = UUID.fromString("a2000026-0026-4026-8026-000000000026");
    public static final UUID CATS_IN_THE_CRADLE = UUID.fromString("a2000027-0027-4027-8027-000000000027");
    public static final UUID LAYLA_ACOUSTIC = UUID.fromString("a2000028-0028-4028-8028-000000000028");
    public static final UUID GIRL_WOMAN_SOON = UUID.fromString("a2000029-0029-4029-8029-000000000029");
    public static final UUID MOONAGE_DAYDREAM = UUID.fromString("a2000030-0030-4030-8030-000000000030");
    public static final UUID STARMAN = UUID.fromString("a2000031-0031-4031-8031-000000000031");
    public static final UUID LIGHT_MY_FIRE = UUID.fromString("a2000032-0032-4032-8032-000000000032");
    public static final UUID SHES_A_RAINBOW = UUID.fromString("a2000033-0033-4033-8033-000000000033");
    public static final UUID FREE_BIRD = UUID.fromString("a2000034-0034-4034-8034-000000000034");
    public static final UUID RAINBOW_IN_THE_DARK = UUID.fromString("a2000035-0035-4035-8035-000000000035");
    public static final UUID TEARS_IN_HEAVEN = UUID.fromString("a2000036-0036-4036-8036-000000000036");
    public static final UUID STRANGLEHOLD = UUID.fromString("a2000037-0037-4037-8037-000000000037");
    public static final UUID HALL_OF_THE_MOUNTAIN_KING = UUID.fromString("a2000038-0038-4038-8038-000000000038");
    public static final UUID WHEN_THE_CROWDS_ARE_GONE = UUID.fromString("a2000039-0039-4039-8039-000000000039");
    public static final UUID SUMMERS_RAIN = UUID.fromString("a2000040-0040-4040-8040-000000000040");
    public static final UUID LONELY_DAY = UUID.fromString("a2000041-0041-4041-8041-000000000041");
    public static final UUID GIRLS_JUST_WANT_TO_HAVE_FUN = UUID.fromString("a2000042-0042-4042-8042-000000000042");
    public static final UUID SINNERMAN = UUID.fromString("a2000043-0043-4043-8043-000000000043");
    public static final UUID BLACK_MAGIC_WOMAN = UUID.fromString("a2000044-0044-4044-8044-000000000044");
    public static final UUID BAD_MOON_RISING = UUID.fromString("a2000045-0045-4045-8045-000000000045");
    public static final UUID MORE_THAN_A_FEELING = UUID.fromString("a2000046-0046-4046-8046-000000000046");
    public static final UUID NEVER_MARRY_A_RAILROAD_MAN = UUID.fromString("a2000047-0047-4047-8047-000000000047");
    public static final UUID STARGAZER = UUID.fromString("a2000048-0048-4048-8048-000000000048");
 
    public static final List<DemoListingCatalog.DemoListing> ALL = List.of(
 
            new DemoListingCatalog.DemoListing(HIGHWAY_STAR_BELTER, "The Expanse: The Collector's Edition",
                    "Various Artists (Cory Todd)", "ASG Records", vinylLp(2), Country.UNITED_STATES, 2019, "NM",
                    96000, List.of("Soundtrack / Spoken Word / Other", "Hard Rock")),
 
            new DemoListingCatalog.DemoListing(HIGHWAY_STAR, "Machine Head", "Deep Purple", "Purple Records",
                    vinylLp(1), Country.UNITED_KINGDOM, 1972, "VG+", 88000, List.of("Hard Rock", "Heavy Metal")),
 
            new DemoListingCatalog.DemoListing(WHEN_A_BLIND_MAN_CRIES, "Singles A's and B's", "Deep Purple",
                    "Purple Records", vinylLp(2), Country.UNITED_KINGDOM, 1993, "NM", 58000,
                    List.of("Hard Rock", "Classic Rock")),
 
            new DemoListingCatalog.DemoListing(CHILD_IN_TIME, "Deep Purple in Rock", "Deep Purple", "Harvest",
                    vinylLp(1), Country.UNITED_KINGDOM, 1970, "VG+", 84000, List.of("Hard Rock", "Heavy Metal")),
 
            new DemoListingCatalog.DemoListing(PERFECT_STRANGERS, "Perfect Strangers", "Deep Purple", "Polydor",
                    vinylLp(1), Country.UNITED_KINGDOM, 1984, "NM", 62000, List.of("Hard Rock", "Heavy Metal")),
 
            new DemoListingCatalog.DemoListing(WONDERFUL_TONIGHT, "Unplugged", "Eric Clapton", "Reprise Records",
                    vinylLp(2), Country.UNITED_KINGDOM, 1992, "NM", 70000, List.of("Blues Rock", "Singer-Songwriter")),
 
            new DemoListingCatalog.DemoListing(TAKE_ME_AWAY, "The Revölution by Night", "Blue Öyster Cult",
                    "Columbia", vinylLp(1), Country.UNITED_STATES, 1983, "VG+", 56000,
                    List.of("Hard Rock", "Classic Rock")),
 
            new DemoListingCatalog.DemoListing(LAST_DAYS_OF_MAY, "Blue Öyster Cult", "Blue Öyster Cult", "Columbia",
                    vinylLp(1), Country.UNITED_STATES, 1972, "VG", 79000, List.of("Hard Rock", "Heavy Metal")),
 
            new DemoListingCatalog.DemoListing(DONT_FEAR_THE_REAPER, "Agents of Fortune", "Blue Öyster Cult",
                    "Columbia", vinylLp(1), Country.UNITED_STATES, 1976, "NM", 91000,
                    List.of("Hard Rock", "Classic Rock")),
 
            new DemoListingCatalog.DemoListing(SPACE_ODDITY, "David Bowie", "David Bowie", "Philips",
                    vinylLp(1), Country.UNITED_KINGDOM, 1969, "VG", 95000, List.of("Folk Rock", "Singer-Songwriter")),
 
            new DemoListingCatalog.DemoListing(ALL_THE_MADMEN, "The Man Who Sold the World", "David Bowie",
                    "Mercury", vinylLp(1), Country.UNITED_KINGDOM, 1970, "VG+", 89000,
                    List.of("Hard Rock", "Glam Rock")),
 
            new DemoListingCatalog.DemoListing(WAR_PIGS, "Paranoid", "Black Sabbath", "Vertigo Records",
                    vinylLp(1), Country.UNITED_KINGDOM, 1970, "VG", 99000, List.of("Heavy Metal", "Doom Metal")),
 
            new DemoListingCatalog.DemoListing(ACHILLES_LAST_STAND, "Presence", "Led Zeppelin", "Swan Song",
                    vinylLp(1), Country.UNITED_KINGDOM, 1976, "VG+", 87000, List.of("Hard Rock", "Classic Rock")),
 
            new DemoListingCatalog.DemoListing(CARRY_ON_WAYWARD_SON, "Leftoverture", "Kansas", "Kirshner",
                    vinylLp(1), Country.UNITED_STATES, 1976, "NM", 78000, List.of("Progressive Rock", "Classic Rock")),
 
            new DemoListingCatalog.DemoListing(CRAZY_ON_YOU, "Dreamboat Annie", "Heart", "Mushroom Records",
                    vinylLp(1), Country.UNITED_STATES, 1976, "VG+", 73000, List.of("Hard Rock", "Folk Rock")),
 
            new DemoListingCatalog.DemoListing(LADY_FANTASY, "Mirage", "Camel", "Deram Records",
                    vinylLp(1), Country.UNITED_KINGDOM, 1974, "VG+", 67000, List.of("Progressive Rock", "Classic Rock")),
 
            new DemoListingCatalog.DemoListing(GATES_OF_BABYLON, "Long Live Rock 'n' Roll", "Rainbow", "Polydor",
                    vinylLp(1), Country.UNITED_KINGDOM, 1978, "NM", 81000, List.of("Heavy Metal", "Hard Rock")),
 
            new DemoListingCatalog.DemoListing(HAPPY_TOGETHER, "Happy Together", "The Turtles", "White Whale Records",
                    vinylLp(1), Country.UNITED_STATES, 1967, "VG", 64000, List.of("Pop", "Classic Rock")),
 
            new DemoListingCatalog.DemoListing(WHILE_MY_GUITAR_RRHOF, "Rock and Roll Hall of Fame: 25th Anniversary Concert",
                    "Various Artists (feat. Tom Petty, Prince, Jeff Lynne, Steve Winwood)", "Columbia",
                    vinylLp(2), Country.UNITED_STATES, 2010, "NM", 69000, List.of("Classic Rock", "Rock")),
 
            new DemoListingCatalog.DemoListing(WATCHTOWER_BSG, "Battlestar Galactica: Season 3", "Bear McCreary",
                    "La-La Land Records", vinylLp(1), Country.UNITED_STATES, 2007, "NM", 74000,
                    List.of("Soundtrack / Spoken Word / Other", "Film Score")),
 
            new DemoListingCatalog.DemoListing(LOVE_IS_LIKE_OXYGEN, "Level Headed", "Sweet", "Polydor",
                    vinylLp(1), Country.UNITED_KINGDOM, 1978, "VG+", 60000, List.of("Glam Rock", "Soft Rock")),
 
            new DemoListingCatalog.DemoListing(SHINE_ON_YOU_CRAZY_DIAMOND, "Wish You Were Here", "Pink Floyd",
                    "Harvest Records", vinylLp(1), Country.UNITED_KINGDOM, 1975, "NM", 98000,
                    List.of("Progressive Rock", "Classic Rock")),
 
            new DemoListingCatalog.DemoListing(MAN_WHO_SOLD_THE_WORLD, "The Man Who Sold the World", "David Bowie",
                    "Mercury", vinylLp(1), Country.UNITED_KINGDOM, 1970, "VG", 89000, List.of("Hard Rock", "Glam Rock")),
 
            new DemoListingCatalog.DemoListing(THE_CHAIN, "Rumours", "Fleetwood Mac", "Warner Bros.",
                    vinylLp(1), Country.UNITED_STATES, 1977, "VG+", 74000, List.of("Soft Rock", "Classic Rock")),
 
            new DemoListingCatalog.DemoListing(HAVE_A_CIGAR, "Wish You Were Here", "Pink Floyd", "Harvest Records",
                    vinylLp(1), Country.UNITED_KINGDOM, 1975, "NM", 98000, List.of("Progressive Rock", "Classic Rock")),
 
            new DemoListingCatalog.DemoListing(WILD_WORLD, "Tea for the Tillerman", "Cat Stevens", "Island Records",
                    vinylLp(1), Country.UNITED_KINGDOM, 1970, "VG+", 76000, List.of("Folk Rock", "Singer-Songwriter")),
 
            new DemoListingCatalog.DemoListing(CATS_IN_THE_CRADLE, "Verities & Balderdash", "Harry Chapin",
                    "Elektra", vinylLp(1), Country.UNITED_STATES, 1974, "VG+", 65000,
                    List.of("Folk Rock", "Singer-Songwriter")),
 
            new DemoListingCatalog.DemoListing(LAYLA_ACOUSTIC, "Unplugged", "Eric Clapton", "Reprise Records",
                    vinylLp(2), Country.UNITED_KINGDOM, 1992, "NM", 70000, List.of("Blues Rock", "Singer-Songwriter")),
 
            new DemoListingCatalog.DemoListing(GIRL_WOMAN_SOON, "Pulp Fiction (Music From the Motion Picture)",
                    "Various Artists (Urge Overkill)", "MCA Records", vinylLp(1), Country.UNITED_STATES, 1994,
                    "NM", 71000, List.of("Soundtrack / Spoken Word / Other", "Alternative Rock")),
 
            new DemoListingCatalog.DemoListing(MOONAGE_DAYDREAM, "The Rise and Fall of Ziggy Stardust and the Spiders From Mars",
                    "David Bowie", "RCA", vinylLp(1), Country.UNITED_KINGDOM, 1972, "VG+", 92000,
                    List.of("Glam Rock", "Classic Rock")),
 
            new DemoListingCatalog.DemoListing(STARMAN, "The Rise and Fall of Ziggy Stardust and the Spiders From Mars",
                    "David Bowie", "RCA", vinylLp(1), Country.UNITED_KINGDOM, 1972, "VG+", 92000,
                    List.of("Glam Rock", "Classic Rock")),
 
            new DemoListingCatalog.DemoListing(LIGHT_MY_FIRE, "The Doors", "The Doors", "Elektra",
                    vinylLp(1), Country.UNITED_STATES, 1967, "VG", 90000, List.of("Psychedelic Rock", "Classic Rock")),
 
            new DemoListingCatalog.DemoListing(SHES_A_RAINBOW, "Their Satanic Majesties Request", "The Rolling Stones",
                    "Decca", vinylLp(1), Country.UNITED_KINGDOM, 1967, "VG", 85000,
                    List.of("Psychedelic Rock", "Classic Rock")),
 
            new DemoListingCatalog.DemoListing(FREE_BIRD, "(Pronounced 'Lĕh-'nérd 'Skin-'nérd)", "Lynyrd Skynyrd",
                    "MCA Records", vinylLp(1), Country.UNITED_STATES, 1973, "NM", 88000,
                    List.of("Southern Rock", "Classic Rock")),
 
            new DemoListingCatalog.DemoListing(RAINBOW_IN_THE_DARK, "Holy Diver", "Dio", "Warner Bros.",
                    vinylLp(1), Country.UNITED_STATES, 1983, "NM", 82000, List.of("Heavy Metal", "Power Metal")),
 
            new DemoListingCatalog.DemoListing(TEARS_IN_HEAVEN, "Unplugged", "Eric Clapton", "Reprise Records",
                    vinylLp(2), Country.UNITED_KINGDOM, 1992, "NM", 70000, List.of("Blues Rock", "Singer-Songwriter")),
 
            new DemoListingCatalog.DemoListing(STRANGLEHOLD, "Ted Nugent", "Ted Nugent", "Epic Records",
                    vinylLp(1), Country.UNITED_STATES, 1975, "VG+", 77000, List.of("Hard Rock", "Classic Rock")),
 
            new DemoListingCatalog.DemoListing(HALL_OF_THE_MOUNTAIN_KING, "Hall of the Mountain King", "Savatage",
                    "Atlantic", vinylLp(1), Country.UNITED_STATES, 1987, "NM", 80000,
                    List.of("Power Metal", "Heavy Metal")),
 
            new DemoListingCatalog.DemoListing(WHEN_THE_CROWDS_ARE_GONE, "Gutter Ballet", "Savatage", "Atlantic",
                    vinylLp(1), Country.UNITED_STATES, 1989, "NM", 78000, List.of("Heavy Metal", "Power Metal")),
 
            new DemoListingCatalog.DemoListing(SUMMERS_RAIN, "Gutter Ballet", "Savatage", "Atlantic",
                    vinylLp(1), Country.UNITED_STATES, 1989, "NM", 78000, List.of("Heavy Metal", "Power Metal")),
 
            new DemoListingCatalog.DemoListing(LONELY_DAY, "Hypnotize", "System of a Down", "American Recordings",
                    vinylLp(2), Country.UNITED_STATES, 2005, "NM", 75000, List.of("Alternative Metal", "Nu Metal")),
 
            new DemoListingCatalog.DemoListing(GIRLS_JUST_WANT_TO_HAVE_FUN, "She's So Unusual", "Cyndi Lauper",
                    "Portrait Records", vinylLp(1), Country.UNITED_STATES, 1983, "NM", 68000,
                    List.of("New Wave / Alternative", "Synth-Pop")),
 
            new DemoListingCatalog.DemoListing(SINNERMAN, "Pastel Blues", "Nina Simone", "Philips",
                    vinylLp(1), Country.UNITED_STATES, 1965, "VG", 93000, List.of("Jazz", "Soul / Funk / R&B")),
 
            new DemoListingCatalog.DemoListing(BLACK_MAGIC_WOMAN, "Abraxas", "Santana", "Columbia",
                    vinylLp(1), Country.UNITED_STATES, 1970, "VG+", 86000, List.of("Latin", "Psychedelic Rock")),
 
            new DemoListingCatalog.DemoListing(BAD_MOON_RISING, "Green River", "Creedence Clearwater Revival",
                    "Fantasy Records", vinylLp(1), Country.UNITED_STATES, 1969, "VG", 84000,
                    List.of("Classic Rock", "Blues Rock")),
 
            new DemoListingCatalog.DemoListing(MORE_THAN_A_FEELING, "Boston", "Boston", "Epic Records",
                    vinylLp(1), Country.UNITED_STATES, 1976, "NM", 90000, List.of("Hard Rock", "Classic Rock")),
 
            new DemoListingCatalog.DemoListing(NEVER_MARRY_A_RAILROAD_MAN, "Scorpio's Dance", "Shocking Blue",
                    "Pink Elephant", vinylLp(1), Country.NETHERLANDS, 1970, "VG+", 61000,
                    List.of("Psychedelic Rock", "Pop")),
 
            new DemoListingCatalog.DemoListing(STARGAZER, "Rising", "Rainbow", "Oyster Records / Polydor",
                    vinylLp(1), Country.UNITED_KINGDOM, 1976, "NM", 83000, List.of("Heavy Metal", "Hard Rock")),
 
            new DemoListingCatalog.DemoListing(DemoListingCatalog.ABBEY_ROAD, "Abbey Road", "The Beatles", "Apple Records", 
                vinylLp(1), Country.UNITED_KINGDOM, 1969, "VG", 90000, List.of("Psychedelic Rock", "Classic Rock")),

            new DemoListingCatalog.DemoListing(DemoListingCatalog.NEVERMIND, "Nevermind", "Nirvana", "DGC", 
                vinylLp(1), Country.UNITED_STATES, 1991, "NM", 78000, List.of("Grunge", "Alternative Rock")),
                
            new DemoListingCatalog.DemoListing(DemoListingCatalog.OK_COMPUTER, "OK Computer", "Radiohead", "Parlophone", 
                vinylLp(1), Country.UNITED_KINGDOM, 1997, "NM", 82000, List.of("Alternative Rock", "Post-Rock")),

            new DemoListingCatalog.DemoListing(DemoListingCatalog.IN_RAINBOWS, "In Rainbows", "Radiohead", "XL Recordings", 
            vinylLp(1), Country.GERMANY, 2007, "NM", 76000, List.of("Alternative Rock", "Indie Rock")),

            new DemoListingCatalog.DemoListing(DemoListingCatalog.THE_WALL, "The Wall", "Pink Floyd", "Harvest", 
            vinylLp(2), Country.UNITED_KINGDOM, 1979, "VG+", 95000, List.of("Progressive Rock", "Classic Rock")),

            new DemoListingCatalog.DemoListing(DemoListingCatalog.KIND_OF_BLUE, "Kind of Blue", "Miles Davis", "Columbia", 
            vinylLp(1), Country.UNITED_STATES, 1959, "VG+", 88000, List.of("Cool Jazz", "Bebop")),

            new DemoListingCatalog.DemoListing(DemoListingCatalog.BLUE_TRAIN, "Blue Train", "John Coltrane", "Blue Note", 
            vinylLp(1), Country.UNITED_STATES, 1957, "VG", 92000, List.of("Bebop", "Cool Jazz")),

            new DemoListingCatalog.DemoListing(DemoListingCatalog.BACK_TO_BLACK, "Back to Black", "Amy Winehouse", "Island Records", 
            vinylLp(1), Country.GERMANY, 2006, "NM", 68000, List.of("Neo-Soul", "Contemporary R&B")),
            
            new DemoListingCatalog.DemoListing(DemoListingCatalog.RUMOURS, "Rumours", "Fleetwood Mac", "Warner Bros.", 
            vinylLp(1), Country.UNITED_STATES, 1977, "VG+", 74000, List.of("Soft Rock", "Classic Rock")),
            
            new DemoListingCatalog.DemoListing(DemoListingCatalog.LED_ZEPPELIN_IV, "Led Zeppelin IV", "Led Zeppelin", "Atlantic", 
            vinylLp(1), Country.UNITED_KINGDOM, 1971, "VG", 86000, List.of("Hard Rock", "Classic Rock")),
            
            new DemoListingCatalog.DemoListing(DemoListingCatalog.MASTER_OF_PUPPETS, "Master of Puppets", "Metallica", "Elektra", 
            vinylLp(1), Country.UNITED_STATES, 1986, "NM", 83000, List.of("Thrash Metal", "Heavy Metal")),
            
            new DemoListingCatalog.DemoListing(DemoListingCatalog.REVOLVER, "Revolver", "The Beatles", "Parlophone", 
            vinylLp(1), Country.UNITED_KINGDOM, 1966, "VG", 89000, List.of("Psychedelic Rock", "Classic Rock")),
            
            new DemoListingCatalog.DemoListing(DemoListingCatalog.UNKNOWN_PLEASURES, "Unknown Pleasures", "Joy Division", "Factory", 
            vinylLp(1), Country.UNITED_KINGDOM, 1979, "NM", 81000, List.of("Post-Punk", "Alternative Rock")),
            
            new DemoListingCatalog.DemoListing(DemoListingCatalog.DISINTEGRATION, "Disintegration", "The Cure", "Fiction", 
            vinylLp(1), Country.UNITED_KINGDOM, 1989, "NM", 79000, List.of("Post-Punk", "Shoegaze")),
            
            new DemoListingCatalog.DemoListing(DemoListingCatalog.ZIGGY_STARDUST, "The Rise and Fall of Ziggy Stardust", "David Bowie", "RCA", 
            vinylLp(1), Country.UNITED_KINGDOM, 1972, "VG+", 84000, List.of("Glam Rock", "Classic Rock")),
            
            new DemoListingCatalog.DemoListing(DemoListingCatalog.DISCOVERY, "Discovery", "Daft Punk", "Virgin", 
            vinylLp(1), Country.GERMANY, 2001, "NM", 72000, List.of("House", "Disco House")),
            
            new DemoListingCatalog.DemoListing(DemoListingCatalog.MEZZANINE, "Mezzanine", "Massive Attack", "Virgin", 
            vinylLp(1), Country.UNITED_KINGDOM, 1998, "NM", 80000, List.of("Trip Hop", "Industrial")),
            
            new DemoListingCatalog.DemoListing(DemoListingCatalog.VIOLATOR, "Violator", "Depeche Mode", "Mute", 
            vinylLp(1), Country.UNITED_KINGDOM, 1990, "VG+", 76000, List.of("Synth-Pop", "Industrial")));
}
