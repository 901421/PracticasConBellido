#include <iostream>
#include <vector>
#include <fstream>
#include <chrono>
#include <algorithm>
#include <numeric>

using namespace std;
using namespace std::chrono;

const int INF = 1e9;

int N;
vector<vector<int>> C;
vector<vector<int>> W; // W[i][j] = C[i][j] + C[j][i]
vector<vector<int>> minW; // minW[i][k] is sum of k smallest W[i][j] for j != i

int best_cost;
long long nodes_generated;

void greedy_init() {
    vector<int> team_of(N, -1);
    vector<vector<int>> teams(N / 3);
    int current_total_cost = 0;
    
    for (int i = 0; i < N; ++i) {
        int best_t = -1;
        int min_added = INF;
        for (int t = 0; t < N / 3; ++t) {
            if (teams[t].size() < 3) {
                int added = 0;
                for (int member : teams[t]) added += W[i][member];
                if (added < min_added) {
                    min_added = added;
                    best_t = t;
                }
                if (teams[t].empty()) break; // Symmetry breaking
            }
        }
        teams[best_t].push_back(i);
        current_total_cost += min_added;
    }
    best_cost = current_total_cost;
}

// current_lb2 is 2 * current_cost + sum of min future Ws for all participants
void solve(int p, int current_cost, int current_lb2, vector<vector<int>>& teams, int active_teams) {
    nodes_generated++;
    
    if (p == N) {
        if (current_cost < best_cost) {
            best_cost = current_cost;
        }
        return;
    }
    
    int lb = (current_lb2 + 1) / 2;
    if (lb >= best_cost) return;
    
    // Decisions for participant p
    struct Choice {
        int team_idx;
        int added_cost;
        bool is_new;
    };
    
    vector<Choice> choices;
    for (int t = 0; t < active_teams; ++t) {
        if (teams[t].size() < 3) {
            int added = 0;
            for (int m : teams[t]) added += W[p][m];
            choices.push_back({t, added, false});
        }
    }
    if (active_teams < N / 3) {
        choices.push_back({active_teams, 0, true});
    }
    
    sort(choices.begin(), choices.end(), [](const Choice& a, const Choice& b) {
        return a.added_cost < b.added_cost;
    });
    
    for (const auto& c : choices) {
        int next_cost = current_cost + c.added_cost;
        
        // Update lb2 for the next state
        int next_lb2 = current_lb2;
        // 1. p is no longer "not yet assigned" (subtract minW[p][2])
        next_lb2 -= minW[p][2];
        
        // 2. p is added to team t
        int t = c.team_idx;
        int sz = teams[t].size();
        // Add 2 * W[p, member] for each member already in team
        for (int m : teams[t]) {
            next_lb2 += 2 * W[p][m];
            // member m now needs one less partner
            int m_needs_before = 3 - sz;
            int m_needs_after = 3 - (sz + 1);
            next_lb2 -= minW[m][m_needs_before];
            next_lb2 += minW[m][m_needs_after];
        }
        // p now needs partners
        int p_needs = 3 - (sz + 1);
        next_lb2 += minW[p][p_needs];
        
        if ((next_lb2 + 1) / 2 < best_cost) {
            teams[t].push_back(p);
            solve(p + 1, next_cost, next_lb2, teams, c.is_new ? active_teams + 1 : active_teams);
            teams[t].pop_back();
        }
    }
}

void process_case(ifstream& in, ofstream& out) {
    C.assign(N, vector<int>(N));
    W.assign(N, vector<int>(N));
    for (int i = 0; i < N; ++i) {
        for (int j = 0; j < N; ++j) {
            in >> C[i][j];
        }
    }
    for (int i = 0; i < N; ++i) {
        for (int j = 0; j < N; ++j) {
            W[i][j] = (i == j) ? 0 : C[i][j] + C[j][i];
        }
    }
    
    minW.assign(N, vector<int>(3, 0));
    for (int i = 0; i < N; ++i) {
        vector<int> row;
        for (int j = 0; j < N; ++j) {
            if (i != j) row.push_back(W[i][j]);
        }
        sort(row.begin(), row.end());
        minW[i][1] = row[0];
        minW[i][2] = row[0] + row[1];
    }
    
    greedy_init();
    nodes_generated = 0;
    
    vector<vector<int>> teams(N / 3);
    
    // Initial lb2: all participants are "not yet assigned", so they each need 2 partners
    int initial_lb2 = 0;
    for (int i = 0; i < N; ++i) initial_lb2 += minW[i][2];
    
    auto start_time = high_resolution_clock::now();
    solve(0, 0, initial_lb2, teams, 0);
    auto end_time = high_resolution_clock::now();
    auto duration = duration_cast<milliseconds>(end_time - start_time).count();
    
    out << duration << " " << nodes_generated << " " << best_cost << endl;
}

int main(int argc, char* argv[]) {
    if (argc != 3) {
        cerr << "Uso: " << argv[0] << " <entrada> <salida>" << endl;
        return 1;
    }
    ifstream in(argv[1]);
    ofstream out(argv[2]);
    while (in >> N) {
        process_case(in, out);
    }
    return 0;
}
