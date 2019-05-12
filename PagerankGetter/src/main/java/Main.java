public class Main {

    public static void main(String[] args) throws Exception {
        Double rank = PageRankConsultant.getInstance().getRankOf("1180d0b0-28b7-4be7-b37d-f44ffb101885");
        String url = PageRankConsultant.getInstance().getUrlOf("1180d0b0-28b7-4be7-b37d-f44ffb101885");
        System.out.println(rank);
        System.out.println(url);
    }
}
