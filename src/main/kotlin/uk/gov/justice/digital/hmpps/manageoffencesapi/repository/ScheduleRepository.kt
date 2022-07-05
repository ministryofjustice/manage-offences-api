package uk.gov.justice.digital.hmpps.manageoffencesapi.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.manageoffencesapi.entity.Schedule
import java.util.Optional

@Repository
interface ScheduleRepository : JpaRepository<Schedule, Long> {
  fun findOneByActAndCode(act: String, code: String): Optional<Schedule>
}
