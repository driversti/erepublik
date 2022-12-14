package com.github.driversti.erepublik.friendsadd;

import static com.github.driversti.erepublik.friendsadd.jobconfigs.Country.DENMARK;
import static com.github.driversti.erepublik.friendsadd.jobconfigs.Country.ESTONIA;
import static com.github.driversti.erepublik.friendsadd.jobconfigs.Country.HUNGARY;
import static com.github.driversti.erepublik.friendsadd.jobconfigs.Country.LATVIA;
import static com.github.driversti.erepublik.friendsadd.jobconfigs.Country.LITHUANIA;
import static com.github.driversti.erepublik.friendsadd.jobconfigs.Country.RUSSIA;
import static com.github.driversti.erepublik.friendsadd.jobconfigs.Country.SERBIA;
import static com.github.driversti.erepublik.friendsadd.jobconfigs.Country.UKRAINE;
import static com.github.driversti.erepublik.friendsadd.jobconfigs.Country.USA;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.driversti.erepublik.friendsadd.clients.ErepublikApiClient;
import com.github.driversti.erepublik.friendsadd.clients.Player;
import com.github.driversti.erepublik.friendsadd.jobconfigs.AddFriendRequestConfig;
import com.github.driversti.erepublik.friendsadd.jobconfigs.GetCitizenRequestConfig;
import com.github.driversti.erepublik.friendsadd.jobconfigs.JobConfig;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class RunnerTest {

  private final ErepublikApiClient erepublikApiClient = Mockito.mock(ErepublikApiClient.class);
  private final Runner runner = new Runner(erepublikApiClient);

  // WARNING! Long-running tests!
  @Test
  void shouldCallAddFriendWhenPlayerIsAmongIncludedCountriesAndIsAlive() {
    // given
    JobConfig jobConfig = new JobConfig.Builder("erpk", "token")
        .fromId(178).toId(179)
        .includedCountries(Set.of(UKRAINE, LITHUANIA, ESTONIA)).build();

    // and
    Player player = new Player().isBanned(false).isDead(false).isBlocked(false)
        .citizenNickname("John Doe").citizenship(UKRAINE);
    when(erepublikApiClient.getCitizen(isA(GetCitizenRequestConfig.class))).thenReturn(player);

    // when
    runner.run(jobConfig);

    // then
    verify(erepublikApiClient, times(2)).addFriend(isA(AddFriendRequestConfig.class));
  }

  @Test
  void shouldCallAddFriendWhenPlayerIsNotAmongExcludedCountriesAndIncludedAreEmpty() {
    // given
    JobConfig jobConfig = new JobConfig.Builder("erpk", "token")
        .fromId(178).toId(178)
        .includedCountries(Set.of())
        .excludedCountries(Set.of(RUSSIA, HUNGARY, SERBIA)).build();

    // and
    Player player = new Player().isBanned(false).isBlocked(false)
        .isDead(false).citizenNickname("John Doe").citizenship(USA);
    when(erepublikApiClient.getCitizen(isA(GetCitizenRequestConfig.class))).thenReturn(player);

    // when
    runner.run(jobConfig);

    // then
    verify(erepublikApiClient, times(1)).addFriend(isA(AddFriendRequestConfig.class));
  }

  @Test
  void shouldNotCallAddFriendWhenPlayerIsBanned() {
    // given
    JobConfig jobConfig = new JobConfig.Builder("erpk", "token")
        .fromId(178).toId(178).build();

    // and
    Player player = new Player().citizenNickname("John Doe").isBanned(true).isBlocked(false)
        .isDead(true);
    when(erepublikApiClient.getCitizen(isA(GetCitizenRequestConfig.class))).thenReturn(player);

    // when
    runner.run(jobConfig);

    // then
    verify(erepublikApiClient, times(0)).addFriend(isA(AddFriendRequestConfig.class));
  }

  @Test
  void shouldNotCallAddFriendWhenPlayerIsDead() {
    // given
    JobConfig jobConfig = new JobConfig.Builder("erpk", "token")
        .fromId(178).toId(178).build();

    // and
    Player player = new Player().isBanned(false).isDead(true).isBlocked(false)
        .citizenNickname("John Doe").citizenship(HUNGARY);
    when(erepublikApiClient.getCitizen(isA(GetCitizenRequestConfig.class))).thenReturn(player);

    // when
    runner.run(jobConfig);

    // then
    verify(erepublikApiClient, times(0)).addFriend(isA(AddFriendRequestConfig.class));
  }

  @Test
  void shouldNotCallAddFriendWhenPlayerIsBlocked() {
    // given
    JobConfig jobConfig = new JobConfig.Builder("erpk", "token")
        .fromId(178).toId(178).build();

    // and
    Player player = new Player().isBanned(false).isBlocked(true)
        .isDead(false).citizenNickname("John Doe").citizenship(HUNGARY);
    when(erepublikApiClient.getCitizen(isA(GetCitizenRequestConfig.class))).thenReturn(player);

    // when
    runner.run(jobConfig);

    // then
    verify(erepublikApiClient, times(0)).addFriend(isA(AddFriendRequestConfig.class));
  }

  @Test
  void shouldNotCallAddFriendWhenPlayerIsAmongExcludedCountries() {
    // given
    JobConfig jobConfig = new JobConfig.Builder("erpk", "token")
        .fromId(178).toId(178).excludedCountries(Set.of(RUSSIA, HUNGARY, SERBIA)).build();

    // and
    Player player = new Player().isBanned(false).isBlocked(true)
        .isDead(false).citizenNickname("John Doe").citizenship(HUNGARY);
    when(erepublikApiClient.getCitizen(isA(GetCitizenRequestConfig.class))).thenReturn(player);

    // when
    runner.run(jobConfig);

    // then
    verify(erepublikApiClient, times(0)).addFriend(isA(AddFriendRequestConfig.class));
  }

  @Test
  void shouldNotCallAddFriendWhenPlayerIsNotAmongAllowedCountries() {
    // given
    JobConfig jobConfig = new JobConfig.Builder("erpk", "token")
        .fromId(178).toId(178).includedCountries(Set.of(UKRAINE, LITHUANIA, LATVIA)).build();

    // and
    Player player = new Player().isBanned(false).isBlocked(false)
        .isDead(false).citizenNickname("Your Father").citizenship(DENMARK);
    when(erepublikApiClient.getCitizen(isA(GetCitizenRequestConfig.class))).thenReturn(player);

    // when
    runner.run(jobConfig);

    // then
    verify(erepublikApiClient, times(0)).addFriend(isA(AddFriendRequestConfig.class));
  }

  @Test
  void shouldNotCallAddFriendWhenPlayerIsNotFound() {
    // given
    JobConfig jobConfig = new JobConfig.Builder("erpk", "token")
        .fromId(178).toId(178).includedCountries(Set.of(UKRAINE, LITHUANIA, LATVIA)).build();

    // and
    when(erepublikApiClient.getCitizen(isA(GetCitizenRequestConfig.class))).thenReturn(null);

    // when
    runner.run(jobConfig);

    // then
    verify(erepublikApiClient, times(0)).addFriend(isA(AddFriendRequestConfig.class));
  }
}
