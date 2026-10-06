package tn.esprit.autoloc.service.impl;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tn.esprit.autoloc.domain.Client;
import tn.esprit.autoloc.repository.IClientRepository;
import tn.esprit.autoloc.service.IClientService;

import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
public class ClientServiceImpl implements IClientService {

    private final IClientRepository clientRepository;

    @Override
    public List<Client> retrieveAllClients() {
        log.info("Retrieving all clients");
        return clientRepository.findAll();
    }

    @Override
    public Client addClient(Client c) {
        log.info("Adding client: {} {}", c.getNom(), c.getPrenom());
        return clientRepository.save(c);
    }

    @Override
    public Client updateClient(Client c) {
        log.info("Updating client id: {}", c.getIdClient());
        return clientRepository.save(c);
    }

    @Override
    public Client retrieveClient(Long idClient) {
        log.info("Retrieving client id: {}", idClient);
        return clientRepository.findById(idClient)
                .orElse(null);
    }

    @Override
    public void removeClient(Long idClient) {
        log.info("Removing client id: {}", idClient);
        clientRepository.deleteById(idClient);
    }

    @Override
    public List<Client> addClients(List<Client> clients) {
        log.info("Adding {} clients", clients.size());
        return clientRepository.saveAll(clients);
    }
}
