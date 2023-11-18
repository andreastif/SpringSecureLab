CREATE TABLE application_users (



                                   id BIGSERIAL NOT NULL,
                                   account_non_expired VARCHAR(255),
                                   account_non_locked VARCHAR(255),
                                   credentials_non_expired VARCHAR(255),
                                   registered_to_client_id VARCHAR(255),
                                   enabled VARCHAR(255),
                                   email VARCHAR(255),
                                   firstname VARCHAR(255),
                                   lastname VARCHAR(255),
                                   password VARCHAR(255),
                                   username VARCHAR(255),
                                   authorities VARCHAR(255),
                                   PRIMARY KEY (id)
);
