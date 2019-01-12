for i in $*; do
  echo -n -e  "$i\t"
  cut -f 1 $i| sed 's/\t/\n/g'  | sort | uniq -c  | awk '$1>1' | wc -l
done
