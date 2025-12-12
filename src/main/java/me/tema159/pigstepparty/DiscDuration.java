package me.tema159.pigstepparty;

public enum DiscDuration {
    _13(3560),
    _CAT(3700),
    _BLOCKS(6900),
    _CHIRP(3700),
    _FAR(3480),
    _MALL(3940),
    _MELLOHI(1920),
    _STAL(3000),
    _STRAD(3760),
    _WARD(5020),
    _11(1420),
    _WAIT(4740),
    _PIGSTEP(2960),
    _OTHERSIDE(3900),
    _CREATOR(3520),
    _CREATOR_MUSIC_BOX(1460),
    _5(3560),
    _RELIC(4380),
    _PRECIPICE(5980),
    _TEARS(3500),
    _LAVA_CHICKEN(2700);

    private final int duration;
    private static final int average = 3454;

    DiscDuration(int durationTicks) {
        this.duration = durationTicks;
    }

    public static int get(String song) {
        try {
            return valueOf(song.substring(10)).duration;
        } catch (IllegalArgumentException | NullPointerException | StringIndexOutOfBoundsException e) {
            return average;
        }
    }

    public static boolean contains(String song) {
        return get(song) != average;
    }
}
