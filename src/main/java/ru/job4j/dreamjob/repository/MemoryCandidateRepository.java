package ru.job4j.dreamjob.repository;

import ru.job4j.dreamjob.model.Candidate;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class MemoryCandidateRepository implements CandidateRepository {

    private static final MemoryCandidateRepository INSTANCE = new MemoryCandidateRepository();

    private int nextId = 1;

    private final Map<Integer, Candidate> Candidates = new HashMap<>();

    private MemoryCandidateRepository() {
        save(new Candidate(0, "Ivan Tarasov", "The best worker", LocalDateTime.now()));
        save(new Candidate(0, "Antonov", "Good worker", LocalDateTime.now()));
        save(new Candidate(0, "Ivanov", "Good worker", LocalDateTime.now()));
        save(new Candidate(0, "Petrov", "Good worker", LocalDateTime.now()));
        save(new Candidate(0, "Sidorov", "Good worker", LocalDateTime.now()));
        save(new Candidate(0, "Kalaudov", "Good worker", LocalDateTime.now()));
    }

    public static MemoryCandidateRepository getInstance() {
        return INSTANCE;
    }

    public Candidate save(Candidate candidate) {
        candidate.setId(nextId++);
        Candidates.put(candidate.getId(), candidate);
        return candidate;
    }

    public void deleteById(int id) {
        Candidates.remove(id);
    }

    public boolean update(Candidate candidate) {
        return Candidates.computeIfPresent(candidate.getId(),
                (id, oldCandidate) -> new Candidate(oldCandidate.getId(), candidate.getName(),
                        candidate.getDescription(),
                        candidate.getCreationDate())) != null;
    }

    public Optional<Candidate> findById(int id) {
        return Optional.ofNullable(Candidates.get(id));
    }


    public Collection<Candidate> findAll() {
        return Candidates.values();
    }
}
