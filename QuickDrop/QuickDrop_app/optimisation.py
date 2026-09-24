
def trouver_solution_optimale(fournisseurs, articles_a_recuperer, distances):
    """
    Recherche la combinaison optimale de fournisseurs.

    Objectif :
        minimiser la distance totale :
        Client -> F1 -> F2 -> ... -> Fn

    Retourne :
        {
            'fournisseurs': [F1, F2, ...],
            'distance': distance_totale,
            'articles': {
                F1: {article1, article2},
                F2: {article3}
            }
        }

    Retourne None si aucun fournisseur ne peut fournir
    au moins une des articles demandés.
    """

    # Vérifier que chaque article est disponible
    # chez au moins un fournisseur
    for article in articles_a_recuperer:

        disponible = any(
            article in data['Articles']
            for data in fournisseurs.values()
        )

        if not disponible:
            return None

    meilleure_distance = float('inf')
    meilleure_solution = None

    def recherche(
        articles_couverts,
        chemin,
        distance_actuelle
    ):

        nonlocal meilleure_distance
        nonlocal meilleure_solution

        # Tous les articles sont couverts
        if articles_a_recuperer <= articles_couverts:

            if distance_actuelle < meilleure_distance:
                meilleure_distance = distance_actuelle
                meilleure_solution = chemin.copy()

            return

        # Branch & Bound
        if distance_actuelle >= meilleure_distance:
            return

        articles_restants = (
            articles_a_recuperer - articles_couverts
        )

        candidats = []

        for fournisseur, data in fournisseurs.items():

            # Ne pas sélectionner deux fois
            # le même fournisseur
            if fournisseur in chemin:
                continue

            nouveaux_articles = (
                data['Articles'] & articles_restants
            )

            if not nouveaux_articles:
                continue

            # Client -> premier fournisseur
            if not chemin:

                cout = data.get(
                    'Distance_Client',
                    float('inf')
                )

            # Fournisseur précédent -> nouveau fournisseur
            else:

                precedent = chemin[-1]

                cout = distances.get(
                    (precedent, fournisseur),
                    float('inf')
                )

            if cout == float('inf'):
                continue

            candidats.append(
                (
                    fournisseur,
                    nouveaux_articles,
                    cout
                )
            )

        # Explorer d'abord les meilleurs candidats
        candidats.sort(
            key=lambda x: x[2] / max(len(x[1]), 1)
        )

        for fournisseur, nouveaux_articles, cout in candidats:

            nouvelle_distance = (
                distance_actuelle + cout
            )

            if nouvelle_distance >= meilleure_distance:
                continue

            recherche(
                articles_couverts | nouveaux_articles,
                chemin + [fournisseur],
                nouvelle_distance
            )

    recherche(
        articles_couverts=set(),
        chemin=[],
        distance_actuelle=0
    )

    if meilleure_solution is None:
        return None

    # Répartir les articles entre les fournisseurs
    articles_restants = set(articles_a_recuperer)

    articles_par_fournisseur = {}

    for fournisseur in meilleure_solution:

        articles = (
            fournisseurs[fournisseur]['Articles']
            & articles_restants
        )

        if articles:

            articles_par_fournisseur[fournisseur] = articles

            articles_restants -= articles

    return {
        'fournisseurs': meilleure_solution,
        'distance': meilleure_distance,
        'articles': articles_par_fournisseur
    }
