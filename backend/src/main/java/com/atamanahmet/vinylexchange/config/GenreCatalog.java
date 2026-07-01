package com.atamanahmet.vinylexchange.config;



import java.util.List;



/**

 * Curated genre taxonomy for startup seeding.

 */

public final class GenreCatalog {



    private GenreCatalog() {

    }



    public record GenreSeed(String name, String parentName, boolean localFlavor) {

    }



    public static final List<GenreSeed> ALL = List.of(

            new GenreSeed("Rock", null, false),

            new GenreSeed("Metal", null, false),

            new GenreSeed("Pop", null, false),

            new GenreSeed("Soul / Funk / R&B", null, false),

            new GenreSeed("Hip Hop", null, false),

            new GenreSeed("Jazz", null, false),

            new GenreSeed("Blues", null, false),

            new GenreSeed("Electronic", null, false),

            new GenreSeed("Classical", null, false),

            new GenreSeed("Folk", null, false),

            new GenreSeed("Country", null, false),

            new GenreSeed("Reggae", null, false),

            new GenreSeed("World / Regional", null, false),

            new GenreSeed("New Wave / Alternative", null, false),

            new GenreSeed("Soundtrack / Spoken Word / Other", null, false),



            new GenreSeed("Classic Rock", "Rock", false),

            new GenreSeed("Hard Rock", "Rock", false),

            new GenreSeed("Soft Rock", "Rock", false),

            new GenreSeed("Psychedelic Rock", "Rock", false),

            new GenreSeed("Progressive Rock", "Rock", false),

            new GenreSeed("Punk Rock", "Rock", false),

            new GenreSeed("Post-Punk", "Rock", false),

            new GenreSeed("Indie Rock", "Rock", false),

            new GenreSeed("Garage Rock", "Rock", false),

            new GenreSeed("Glam Rock", "Rock", false),

            new GenreSeed("Southern Rock", "Rock", false),

            new GenreSeed("Grunge", "Rock", false),

            new GenreSeed("Post-Rock", "Rock", false),

            new GenreSeed("Stoner Rock", "Rock", false),

            new GenreSeed("Emo", "Rock", false),



            new GenreSeed("Heavy Metal", "Metal", false),

            new GenreSeed("Thrash Metal", "Metal", false),

            new GenreSeed("Death Metal", "Metal", false),

            new GenreSeed("Black Metal", "Metal", false),

            new GenreSeed("Doom Metal", "Metal", false),

            new GenreSeed("Power Metal", "Metal", false),

            new GenreSeed("Nu Metal", "Metal", false),

            new GenreSeed("Alternative Metal", "Metal", false),

            new GenreSeed("Metalcore", "Metal", false),

            new GenreSeed("Industrial Metal", "Metal", false),

            new GenreSeed("Gothic Metal", "Metal", false),

            new GenreSeed("Sludge Metal", "Metal", false),



            new GenreSeed("Synth-Pop", "Pop", false),

            new GenreSeed("Disco", "Pop", false),

            new GenreSeed("Dance-Pop", "Pop", false),

            new GenreSeed("Indie Pop", "Pop", false),

            new GenreSeed("Power Pop", "Pop", false),

            new GenreSeed("Europop", "Pop", false),

            new GenreSeed("K-Pop", "Pop", false),

            new GenreSeed("J-Pop", "Pop", false),

            new GenreSeed("Electropop", "Pop", false),

            new GenreSeed("Teen Pop", "Pop", false),



            new GenreSeed("Funk", "Soul / Funk / R&B", false),

            new GenreSeed("Motown", "Soul / Funk / R&B", false),

            new GenreSeed("Northern Soul", "Soul / Funk / R&B", false),

            new GenreSeed("Neo-Soul", "Soul / Funk / R&B", false),

            new GenreSeed("Contemporary R&B", "Soul / Funk / R&B", false),

            new GenreSeed("Quiet Storm", "Soul / Funk / R&B", false),

            new GenreSeed("Disco-Funk", "Soul / Funk / R&B", false),

            new GenreSeed("New Jack Swing", "Soul / Funk / R&B", false),

            new GenreSeed("Philly Soul", "Soul / Funk / R&B", false),



            new GenreSeed("Old School Hip Hop", "Hip Hop", false),

            new GenreSeed("Boom Bap", "Hip Hop", false),

            new GenreSeed("Gangsta Rap", "Hip Hop", false),

            new GenreSeed("Trap", "Hip Hop", false),

            new GenreSeed("Conscious Hip Hop", "Hip Hop", false),

            new GenreSeed("West Coast Hip Hop", "Hip Hop", false),

            new GenreSeed("Dirty South", "Hip Hop", false),

            new GenreSeed("Drill", "Hip Hop", false),

            new GenreSeed("Mumble Rap", "Hip Hop", false),



            new GenreSeed("Bebop", "Jazz", false),

            new GenreSeed("Cool Jazz", "Jazz", false),

            new GenreSeed("Smooth Jazz", "Jazz", false),

            new GenreSeed("Fusion", "Jazz", false),

            new GenreSeed("Free Jazz", "Jazz", false),

            new GenreSeed("Swing", "Jazz", false),

            new GenreSeed("Big Band", "Jazz", false),

            new GenreSeed("Vocal Jazz", "Jazz", false),

            new GenreSeed("Latin Jazz", "Jazz", false),



            new GenreSeed("Delta Blues", "Blues", false),

            new GenreSeed("Chicago Blues", "Blues", false),

            new GenreSeed("Electric Blues", "Blues", false),

            new GenreSeed("Blues Rock", "Blues", false),



            new GenreSeed("House", "Electronic", false),

            new GenreSeed("Techno", "Electronic", false),

            new GenreSeed("Trance", "Electronic", false),

            new GenreSeed("Drum and Bass", "Electronic", false),

            new GenreSeed("Dubstep", "Electronic", false),

            new GenreSeed("Ambient", "Electronic", false),

            new GenreSeed("Synthwave", "Electronic", false),

            new GenreSeed("Disco House", "Electronic", false),

            new GenreSeed("IDM", "Electronic", false),

            new GenreSeed("Trip Hop", "Electronic", false),

            new GenreSeed("Industrial", "Electronic", false),

            new GenreSeed("Eurodance", "Electronic", false),

            new GenreSeed("Downtempo", "Electronic", false),



            new GenreSeed("Baroque", "Classical", false),

            new GenreSeed("Romantic", "Classical", false),

            new GenreSeed("Opera", "Classical", false),

            new GenreSeed("Contemporary Classical", "Classical", false),

            new GenreSeed("Chamber Music", "Classical", false),



            new GenreSeed("Singer-Songwriter", "Folk", false),

            new GenreSeed("Folk Rock", "Folk", false),

            new GenreSeed("World Folk", "Folk", false),

            new GenreSeed("Americana", "Folk", false),

            new GenreSeed("Contemporary Folk", "Folk", false),

            new GenreSeed("Indie Folk", "Folk", false),



            new GenreSeed("Classic Country", "Country", false),

            new GenreSeed("Country Rock", "Country", false),

            new GenreSeed("Outlaw Country", "Country", false),

            new GenreSeed("Bluegrass", "Country", false),

            new GenreSeed("Country Pop", "Country", false),



            new GenreSeed("Dub", "Reggae", false),

            new GenreSeed("Ska", "Reggae", false),

            new GenreSeed("Dancehall", "Reggae", false),

            new GenreSeed("Roots Reggae", "Reggae", false),

            new GenreSeed("Lovers Rock", "Reggae", false),



            new GenreSeed("Latin", "World / Regional", false),

            new GenreSeed("Afrobeat", "World / Regional", false),

            new GenreSeed("Turkish Pop", "World / Regional", true),

            new GenreSeed("Arabesk", "World / Regional", true),

            new GenreSeed("Anatolian Rock", "World / Regional", true),

            new GenreSeed("Bossa Nova", "World / Regional", false),

            new GenreSeed("Flamenco", "World / Regional", false),



            new GenreSeed("Alternative Rock", "New Wave / Alternative", false),

            new GenreSeed("Britpop", "New Wave / Alternative", false),

            new GenreSeed("Shoegaze", "New Wave / Alternative", false),



            new GenreSeed("Film Score", "Soundtrack / Spoken Word / Other", false),

            new GenreSeed("Spoken Word", "Soundtrack / Spoken Word / Other", false),

            new GenreSeed("Comedy", "Soundtrack / Spoken Word / Other", false),

            new GenreSeed("Children's Music", "Soundtrack / Spoken Word / Other", false));

}
