DROP TABLE GLOBAL_CONFIG IF EXISTS;
DROP TABLE GLOBAL_FILTER IF EXISTS;
DROP TABLE USER_CONFIG IF EXISTS;
DROP TABLE USER_FILTER IF EXISTS;

CREATE TABLE GLOBAL_CONFIG (
    ID integer identity primary key,  
    ENTITY varchar(100) not null,
    ENTITY_PROPERTIES varchar(500) not null,
    HIDDEN_PROPERTIES varchar(500),
    ORDERBY varchar(500),    
    WHEREFILTER_FIX varchar(500),
    INFOFILTER_FIX varchar(500),
	CONSTRAINT GLOBAL_CONFIG_ID PRIMARY KEY (ID)
);

CREATE TABLE GLOBAL_FILTER (
    ID integer identity primary key,  
    ENTITY varchar(100) not null,
    WHEREFILTER varchar(500),
    INFOFILTER varchar(500),
    LABELFILTER varchar(500),
    BYDEFAULT varchar(1),
	CONSTRAINT GLOBAL_FILTER_ID PRIMARY KEY (ID)
);

CREATE TABLE USER_CONFIG (
    ID integer identity primary key,
    IDUSER varchar(50) NOT NULL,
    ENTITY varchar(100) not null,
    ENTITY_PROPERTIES varchar(500) not null,    
    WHEREFILTER varchar(500),
    INFOFILTER varchar(500),
    ORDERBY varchar(500),
    GROUPBY varchar (500),
	CONSTRAINT USER_CONFIG_ID PRIMARY KEY (ID),
	CONSTRAINT FK_USER_CONFIG_IDUSER FOREIGN KEY (IDUSER) REFERENCES USERS(USERNAME) on delete cascade
);

CREATE TABLE USER_FILTER (
    ID integer identity primary key,
    IDUSER varchar(50) NOT NULL,
    ENTITY varchar(100) not null,
    WHEREFILTER varchar(500),
    INFOFILTER varchar(500),
    LABELFILTER varchar(500),    
	CONSTRAINT USER_FILTER_ID PRIMARY KEY (ID),
	CONSTRAINT FK_USER_FILTER_IDUSER FOREIGN KEY (IDUSER) REFERENCES USERS(USERNAME) on delete cascade
);
