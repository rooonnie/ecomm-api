package com.rooonnie.ecomm.rereel;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RereelJobRepository extends JpaRepository<RereelJob, Long> {

    List<RereelJob> findAllByOrderByIdDesc();
}
