package ru.job4j.dreamjob.repository;

import net.jcip.annotations.ThreadSafe;
import org.springframework.stereotype.Repository;
import ru.job4j.dreamjob.model.Candidate;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Repository
@ThreadSafe
public class MemoryCandidateRepository implements CandidateRepository {

    private int nextId = 1;

    private final Map<Integer, Candidate> candidates = new HashMap<>();

    private MemoryCandidateRepository() {
        save(new Candidate(0, "Ivan Tarasov", "The best worker", LocalDateTime.now()));
        save(new Candidate(0, "Antonov", "Good worker", LocalDateTime.now()));
        save(new Candidate(0, "Ivanov", "Good worker", LocalDateTime.now()));
        save(new Candidate(0, "Petrov", "Good worker", LocalDateTime.now()));
        save(new Candidate(0, "Sidorov", "Good worker", LocalDateTime.now()));
        save(new Candidate(0, "Kalaudov", "Good worker", LocalDateTime.now()));
    }

    public Candidate save(Candidate candidate) {
        candidate.setId(nextId++);
        candidates.put(candidate.getId(), candidate);
        return candidate;
    }

    public void deleteById(int id) {
        candidates.remove(id);
    }

    public boolean update(Candidate candidate) {
        return candidates.computeIfPresent(candidate.getId(),
                (id, oldCandidate) -> new Candidate(oldCandidate.getId(), candidate.getName(),
                        candidate.getDescription(),
                        candidate.getCreationDate())) != null;
    }

    public Optional<Candidate> findById(int id) {
        return Optional.ofNullable(candidates.get(id));
    }

    public Collection<Candidate> findAll() {
        return candidates.values();
    }
}
