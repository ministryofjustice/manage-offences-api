package uk.gov.justice.digital.hmpps.manageoffencesapi.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.manageoffencesapi.entity.Offence
import java.util.Optional

@Repository
interface OffenceRepository : JpaRepository<Offence, Long> {
  fun findByCodeStartsWithIgnoreCase(code: String): List<Offence>
  fun findOneByCode(code: String): Optional<Offence>
}
