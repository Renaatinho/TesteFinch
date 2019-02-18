CREATE TABLE `partidas` (
  `id` INT NOT NULL,
  `data_partida` DATETIME NOT NULL,
  `campeonato` VARCHAR(45) NOT NULL,
  `time_mandante` VARCHAR(45) NOT NULL,
  `placar_time_mandante` INT UNSIGNED NULL,
  `time_visitante` VARCHAR(45) NOT NULL,
  `placar_time_visitante` INT UNSIGNED NULL,
  PRIMARY KEY (`id`));
