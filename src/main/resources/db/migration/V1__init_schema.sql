CREATE TABLE members (
                         id BIGSERIAL NOT NULL,
                         uuid UUID UNIQUE,
                         username VARCHAR(255) UNIQUE,
                         email VARCHAR(255) UNIQUE,
                         created TIMESTAMP WITH TIME ZONE,
                         last_updated TIMESTAMP WITH TIME ZONE,
                         password VARCHAR(255),
                         firstname VARCHAR(255),
                         lastname VARCHAR(255),
                         registered_to_client_id VARCHAR(255),
                         account_non_expired BOOLEAN,
                         account_non_locked BOOLEAN,
                         credentials_non_expired BOOLEAN,
                         enabled BOOLEAN,
                         PRIMARY KEY (id)
);


CREATE TABLE roles (
                       id BIGSERIAL NOT NULL,
                       role_name VARCHAR(255) UNIQUE,
                       PRIMARY KEY (id)
);


CREATE TABLE member_role_map (
                                 id BIGSERIAL NOT NULL,
                                 member_id BIGINT NOT NULL,
                                 role_id BIGINT NOT NULL,
                                 PRIMARY KEY (id),
                                 FOREIGN KEY (member_id) REFERENCES members(id),
                                 FOREIGN KEY (role_id) REFERENCES roles(id)
);

