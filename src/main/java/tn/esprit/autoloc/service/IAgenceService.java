package tn.esprit.autoloc.service;

import tn.esprit.autoloc.domain.Agence;

import java.util.List;

public interface IAgenceService {
    List<Agence> retrieveAllAgences();
    Agence addAgence(Agence a);
    Agence updateAgence(Agence a);
    Agence retrieveAgence(Long idAgence);
    void removeAgence(Long idAgence);
}
