import java.util.*;

class Film {
    String title;
    String genre;

    Film(String title, String genre) {
        this.title = title;
        this.genre = genre;
    }
}

class User {
    String name;
    List<String> watchedTitles;

    User(String name, List<String> watchedTitles) {
        this.name = name;
        this.watchedTitles = watchedTitles;
    }
}

class NaiveBayesClassifier {
    Map<String, Integer> genreCount = new HashMap<>();
    Map<String, Map<String, Integer>> filmGivenGenre = new HashMap<>();
    int totalGenres = 0;

    void train(List<User> users, List<Film> filmList) {
        for (User user : users) {
            for (String title : user.watchedTitles) {
                String genre = getGenre(title, filmList);
                if (genre == null) continue;

                genreCount.put(genre, genreCount.getOrDefault(genre, 0) + 1);
                filmGivenGenre.putIfAbsent(genre, new HashMap<>());
                Map<String, Integer> films = filmGivenGenre.get(genre);
                films.put(title, films.getOrDefault(title, 0) + 1);
                totalGenres++;
            }
        }
    }

    String predict(List<String> watched, List<Film> filmList) {
        String bestGenre = null;
        double bestProb = -1.0;

        for (String genre : genreCount.keySet()) {
            double probGenre = (double) genreCount.get(genre) / totalGenres;
            double probGivenFilms = 1.0;

            for (String title : watched) {
                Map<String, Integer> filmFreq = filmGivenGenre.getOrDefault(genre, new HashMap<>());
                int count = filmFreq.getOrDefault(title, 0) + 1; // Laplace smoothing
                int total = genreCount.get(genre) + filmFreq.size();
                probGivenFilms *= (double) count / total;
            }

            double finalProb = probGenre * probGivenFilms;
            if (finalProb > bestProb) {
                bestProb = finalProb;
                bestGenre = genre;
            }
        }

        return bestGenre;
    }

    String getGenre(String title, List<Film> filmList) {
        for (Film film : filmList) {
            if (film.title.equalsIgnoreCase(title)) {
                return film.genre;
            }
        }
        return null;
    }
}

class ClassifierWithRecommendation extends NaiveBayesClassifier {
    List<Film> filmList;

    ClassifierWithRecommendation(List<Film> filmList) {
        this.filmList = filmList;
    }

    void recommendFilms(String predictedGenre, List<String> watched) {
        System.out.println("\nGenre yang kemungkinan disukai: " + predictedGenre);
        System.out.println("Rekomendasi film:");

        boolean found = false;
        for (Film film : filmList) {
            if (film.genre.equalsIgnoreCase(predictedGenre) && !watched.contains(film.title)) {
                System.out.println("- " + film.title + " (Genre: " + film.genre + ")");
                found = true;
            }
        }

        if (!found) {
            System.out.println("Tidak ada film baru untuk direkomendasikan dalam genre tersebut.");
        }
    }
}

public class adamfilm {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        List<Film> films = List.of(
            new Film("Inception", "Sci-Fi"),
            new Film("Interstellar", "Sci-Fi"),
            new Film("Titanic", "Romance"),
            new Film("The Notebook", "Romance"),
            new Film("The Conjuring", "Horror"),
            new Film("Annabelle", "Horror"),
            new Film("John Wick", "Action"),
            new Film("Mad Max", "Action")
        );

        List<User> trainingUsers = List.of(
            new User("Ayu", List.of("Titanic", "The Notebook")),
            new User("Budi", List.of("Inception", "Interstellar")),
            new User("Cici", List.of("The Conjuring", "Annabelle")),
            new User("Deni", List.of("John Wick", "Mad Max")),
            new User("Eka", List.of("Interstellar", "Inception"))
        );

        System.out.print("Masukkan nama Anda: ");
        String name = scanner.nextLine();

        System.out.print("Masukkan jumlah film yang telah Anda tonton: ");
        int n = scanner.nextInt(); scanner.nextLine();

        List<String> watched = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            System.out.print("Film ke-" + (i + 1) + ": ");
            watched.add(scanner.nextLine());
        }

        ClassifierWithRecommendation classifier = new ClassifierWithRecommendation(films);
        classifier.train(trainingUsers, films);
        String predictedGenre = classifier.predict(watched, films);
        classifier.recommendFilms(predictedGenre, watched);
    }
}
